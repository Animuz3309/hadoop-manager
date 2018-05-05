package edu.scut.cs.hm.model.container;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import edu.scut.cs.hm.admin.component.ContainerCreator;
import edu.scut.cs.hm.common.utils.SingleValueCache;
import edu.scut.cs.hm.docker.DockerService;
import edu.scut.cs.hm.docker.arg.*;
import edu.scut.cs.hm.docker.cmd.UpdateContainerCmd;
import edu.scut.cs.hm.docker.model.container.ContainerDetails;
import edu.scut.cs.hm.docker.model.container.DockerContainer;
import edu.scut.cs.hm.docker.model.container.EditableContainerSource;
import edu.scut.cs.hm.docker.model.swarm.Service;
import edu.scut.cs.hm.docker.model.swarm.Task;
import edu.scut.cs.hm.docker.res.CreateAndStartContainerResult;
import edu.scut.cs.hm.docker.res.ResultCode;
import edu.scut.cs.hm.docker.res.ServiceCallResult;
import edu.scut.cs.hm.model.cluster.DockerCluster;
import edu.scut.cs.hm.model.cluster.SwarmUtils;
import edu.scut.cs.hm.model.node.NodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Containers manager for swarm-mode clusters. <p/>
 * We must prevent managing of containers which is enclosed to existed 'service'.
 */
@Slf4j
public class DockerClusterContainers implements ContainersManager {

    protected final DockerCluster dc;
    protected final ContainerStorage containerStorage;
    protected final SingleValueCache<Map<String, ContainerService>> svcmap;
    protected final SingleValueCache<Map<String, List<Task>>> tasksmap;
    private final ContainerCreator containerCreator;

    public DockerClusterContainers(DockerCluster dc, ContainerStorage containerStorage, ContainerCreator containerCreator) {
        this.dc = dc;
        this.containerStorage = containerStorage;
        this.containerCreator = containerCreator;
        this.svcmap = SingleValueCache.builder(this::loadServices)
                .timeAfterWrite(TimeUnit.SECONDS, dc.getConfig().getConfig().getCacheTimeAfterWrite())
                .build();
        this.tasksmap = SingleValueCache.builder(this::loadTasks)
                .timeAfterWrite(TimeUnit.SECONDS, dc.getConfig().getConfig().getCacheTimeAfterWrite())
                .build();
    }

    private Map<String, List<Task>> loadTasks() {
        List<Task> tasks = getDocker().getTasks(new GetTasksArg());
        Map<String, List<Task>> tps = new HashMap<>();
        tasks.forEach(t -> tps.computeIfAbsent(t.getServiceId(), id -> new ArrayList<>()).add(t));
        return tps;
    }

    private Map<String, ContainerService> loadServices() {
        List<Service> services = getDocker().getServices(new GetServicesArg());
        ImmutableMap.Builder<String, ContainerService> ilb = ImmutableMap.builder();
        Map<String, List<Task>> tpsmap = tasksmap.get();
        services.forEach((s) -> ilb.put(s.getId(), convertService(s, tpsmap.get(s.getId()))));
        return ilb.build();
    }

    private ContainerService convertService(Service s, List<Task> tasks) {
        ContainerService.Builder csb = ContainerService.builder();
        csb.setCluster(dc.getName());
        csb.setService(s);
        csb.setTasks(tasks);
        return csb.build();
    }

    protected DockerService getDocker() {
        DockerService service = this.dc.getDocker();
        Assert.notNull(service, "Cluster return null docker value");
        return service;
    }

    /**
     * Collection of swarm-mode services. Not all clusters support this, then return empty collection.
     *
     * @return non null collection
     */
    @Override
    public Collection<ContainerService> getServices() {
        return svcmap.get().values();
    }

    /**
     * Collection of all containers (include service tasks and standalone containers)
     *
     * @return non null collection
     */
    @Override
    public Collection<DockerContainer> getContainers() {
        return getContainersInternal();
    }

    private List<DockerContainer> getContainersInternal() {
        ImmutableList.Builder<DockerContainer> conts = ImmutableList.builder();
        List<ContainerRegistration> crs = containerStorage.getContainers();
        crs.forEach((cr) -> {
            DockerContainer container = cr.getContainer();
            if(this.dc.hasNode(container.getNode())) {
                conts.add(container);
            }
        });
        return conts.build();
    }

    /**
     * Create container. Note that it created on
     *
     * @param arg argument
     * @return container creation result
     */
    @Override
    public CreateAndStartContainerResult createContainer(CreateContainerArg arg) {
        DockerService ds = getNodeForNew(arg);
        return containerCreator.createContainer(arg, ds);
    }

    private DockerService getNodeForNew(CreateContainerArg arg) {
        String node = arg.getContainer().getNode();
        if(node == null || !dc.hasNode(node)) {
            // we use dummy random strategy
            // from one side if you need good scheduling you must create 'service'
            // from other we support legacy contract when user can schedule container
            List<NodeInfo> nodes = dc.getNodes();
            int num = (int) (Math.random() * (nodes.size() - 1));
            NodeInfo nodeInfo = nodes.get(num);
            node = nodeInfo.getName();
        }
        return getNodeService(node);
    }

    private DockerService getNodeService(String node) {
        DockerService ds = dc.getNodeStorage().getDockerService(node);
        Assert.notNull(ds, "Can not find docker service for node: " + node);
        return ds;
    }

    /**
     * Update (edit) container.
     *
     * @param arg arg
     * @return update container result
     */
    @Override
    public ServiceCallResult updateContainer(EditContainerArg arg) {
        String containerId = arg.getContainerId();
        DockerService ds = getContainerDocker(containerId);
        UpdateContainerCmd cmd = new UpdateContainerCmd();
        EditableContainerSource src = arg.getSource();
        cmd.from(src);
        cmd.setId(containerId);
        return ds.updateContainer(cmd);
    }

    private DockerService getContainerDocker(String containerId) {
        ContainerRegistration container = containerStorage.getContainer(containerId);
        return getNodeService(container.getNode());
    }

    @Override
    public ServiceCallResult stopContainer(StopContainerArg arg) {
        DockerService ds = getContainerDocker(arg.getId());
        return ds.stopContainer(arg);
    }

    @Override
    public ServiceCallResult restartContainer(StopContainerArg arg) {
        DockerService ds = getContainerDocker(arg.getId());
        return ds.restartContainer(arg);
    }

    @Override
    public ServiceCallResult startContainer(String containerId) {
        DockerService ds = getContainerDocker(containerId);
        return ds.startContainer(containerId);
    }

    @Override
    public ServiceCallResult pauseContainer(String containerId) {
        DockerService ds = getContainerDocker(containerId);
        return ds.pauseContainer(containerId);
    }

    @Override
    public ServiceCallResult unpauseContainer(String containerId) {
        DockerService ds = getContainerDocker(containerId);
        return ds.unpauseContainer(containerId);
    }

    @Override
    public ServiceCallResult deleteContainer(DeleteContainerArg arg) {
        DockerService ds = getContainerDocker(arg.getId());
        return ds.deleteContainer(arg);
    }

    @Override
    public ServiceCallResult scaleContainer(ScaleContainerArg arg) {
        String containerId = arg.getContainerId();
        ContainerRegistration cr = containerStorage.getContainer(containerId);
        if(cr == null) {
            return new ServiceCallResult().code(ResultCode.NOT_FOUND).message(containerId + " is not registered");
        }
        String serviceId = cr.getContainer().getLabels().get(SwarmUtils.LABEL_SERVICE_ID);
        if(serviceId != null) {
            return scaleService(serviceId, arg.getScale());
        }
        throw new UnsupportedOperationException("Not implemented yet.");
        // we currently not support this because scale need strategy for spread containers between nodes
        // therefore user must create service, or in future we implement this
        //DockerService ds = getContainerDocker(arg.getContainerId());
        //containerCreator.scale(ds, arg.getScale(), arg.getContainerId());
    }

    private ServiceCallResult scaleService(String serviceId, int scale) {
        DockerService docker = dc.getDocker();
        Service service = docker.getService(serviceId);
        Service.ServiceSpec origSpec = service.getSpec();
        UpdateServiceArg arg = new UpdateServiceArg();
        arg.setVersion(service.getVersion().getIndex());
        arg.setService(serviceId);
        Service.ServiceSpec.Builder ss = origSpec.toBuilder();
        ss.mode(Service.ServiceMode.builder().replicated(new Service.ReplicatedService(scale)).build());
        arg.setSpec(ss.build());
        return docker.updateService(arg);
    }

    @Override
    public ContainerDetails getContainer(String id) {
        DockerService ds = getContainerDocker(id);
        return ds.getContainer(id);
    }

    @Override
    public ContainerService getService(String id) {
        Service service = getDocker().getService(id);
        if(service == null) {
            return null;
        }
        Map<String, List<Task>> tpsmap = tasksmap.get();
        // do not use id from method parameters here, because it may be name of service
        return convertService(service, tpsmap.get(service.getId()));
    }

    /**
     * Create service, when ngroup does not supported services underline code emulate them.
     *
     * @param arg argument
     * @return service creation result
     */
    @Override
    public ServiceCallResult createService(CreateServiceArg arg) {
        return getDocker().createService(arg);
    }

    /**
     * Update (edit) service.
     *
     * @param arg arg
     * @return update service result
     */
    @Override
    public ServiceCallResult updateService(UpdateServiceArg arg) {
        return getDocker().updateService(arg);
    }

    @Override
    public ServiceCallResult deleteService(String service) {
        return getDocker().deleteService(service);
    }
}
