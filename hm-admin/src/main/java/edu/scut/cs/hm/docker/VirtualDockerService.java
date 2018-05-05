package edu.scut.cs.hm.docker;

import edu.scut.cs.hm.admin.service.NodeStorage;
import edu.scut.cs.hm.docker.arg.*;
import edu.scut.cs.hm.docker.cmd.*;
import edu.scut.cs.hm.docker.model.container.ContainerDetails;
import edu.scut.cs.hm.docker.model.container.DockerContainer;
import edu.scut.cs.hm.docker.model.image.ImageDescriptor;
import edu.scut.cs.hm.docker.model.image.ImageItem;
import edu.scut.cs.hm.docker.model.network.Network;
import edu.scut.cs.hm.docker.model.swarm.Service;
import edu.scut.cs.hm.docker.res.SwarmInspectResponse;
import edu.scut.cs.hm.docker.model.swarm.SwarmNode;
import edu.scut.cs.hm.docker.model.swarm.Task;
import edu.scut.cs.hm.docker.model.volume.Volume;
import edu.scut.cs.hm.docker.res.*;
import edu.scut.cs.hm.model.cluster.DefaultCluster;
import edu.scut.cs.hm.model.node.Node;
import edu.scut.cs.hm.model.node.NodeInfo;
import edu.scut.cs.hm.model.node.NodeUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Docker service for virtual nodes group (not united by docker in swarm mode or swarm)
 * This service will get actual docker service from the node where the container in by {@link #getServiceByContainer(String)},
 * and then delegate operations to the actual docker service
 */
public class VirtualDockerService implements DockerService {

    public static boolean isOffline(DockerService service) {
        return service == null || !service.isOnline();
    }

    /**
     * Get the Name of ngroup {@link DefaultCluster}
     * Note: only one of {@link #getCluster()} or {@link #getNode()} can has non null value
     * @return not null
     */
    @Override
    public String getCluster() {
        return this.cluster.getName();
    }

    /**
     * Name of node
     * Note: only one of {@link #getCluster()} or {@link #getNode()} can has non null value
     * @return null
     */
    @Override
    public String getNode() {
        return null;
    }

    /**
     * Address of docker service in 'ip:port' format, sometime it can be null.
     * Here is a nodes group's docker service not the single one
     * virtual service not has address
     * @return null
     */
    @Override
    public String getAddress() {
        return null;
    }

    /**
     * Docker service is online or not
     * @return always return true
     */
    @Override
    public boolean isOnline() {
        return true;
    }

    /**
     * Retrieve details info about one container.
     *
     * @param id
     * @return container details or null if not found (docker service the container with 'id' in is null or offline)
     */
    @Override
    public ContainerDetails getContainer(String id) {
        DockerService service = getServiceByContainer(id);
        if (isOffline(service)) {
            return null;
        }
        return service.getContainer(id);
    }

    /**
     * Retrieve list of Docker containers
     *
     * @param arg
     * @return
     */
    @Override
    public List<DockerContainer> getContainers(GetContainersArg arg) {
        return this.cluster.getContainersImpl(arg);
    }

    /**
     * Get container stats based on resource usage
     *
     * @param arg
     * @return
     */
    @Override
    public ServiceCallResult getStatistics(GetStatisticsArg arg) {
        String id = arg.getId();
        DockerService service = getServiceByContainer(id);
        if (isOffline(service)) {
            return whenNotFoundService(id);
        }
        return service.getStatistics(arg);
    }

    /**
     * Display system-wide information
     *
     * @return info just has information of nodes and number of offline nodes(node is not on or docker service is offline)
     */
    @Override
    public DockerServiceInfo getInfo() {
        List<NodeInfo> nodeList = new ArrayList<>();
        int offNodes = 0;
        for (NodeInfo nodeInfo: cluster.getNodes()) {
            if(nodeInfo != null) {
                nodeList.add(nodeInfo);
            }
            // if nodeInfo is null we can find the exception here
            DockerService service = getServiceByNode(nodeInfo);
            if(isOffline(service)) {
                offNodes++;
                // due to different causes service can be null
                continue;
            }
            // nodeInfo == null above will throw exception
            if(nodeInfo == null || !nodeInfo.isOn()) {
                offNodes++;
            }
        }
        return DockerServiceInfo.builder()
                .name(getCluster())
                .nodeList(nodeList)
                .nodeCount(nodeList.size() - offNodes)
                .offNodeCount(offNodes)
                .build();
    }

    /**
     * Start specified by id container
     *
     * @param id id of container
     * @return result
     */
    @Override
    public ServiceCallResult startContainer(String id) {
        DockerService service = getServiceByContainer(id);
        if (isOffline(service)) {
            return whenNotFoundService(id);
        }
        return service.startContainer(id);
    }

    /**
     * Pause specified by id container
     *
     * @param id id of container
     * @return result
     */
    @Override
    public ServiceCallResult pauseContainer(String id) {
        DockerService service = getServiceByContainer(id);
        if(isOffline(service)) {
            return whenNotFoundService(id);
        }
        return service.pauseContainer(id);
    }

    /**
     * Run previously paused container
     *
     * @param id id of container
     * @return result
     */
    @Override
    public ServiceCallResult unpauseContainer(String id) {
        DockerService service = getServiceByContainer(id);
        if(isOffline(service)) {
            return whenNotFoundService(id);
        }
        return service.unpauseContainer(id);
    }

    /**
     * Stop specified by id container
     *
     * @param arg
     * @return result
     */
    @Override
    public ServiceCallResult stopContainer(StopContainerArg arg) {
        DockerService service = getServiceByContainer(arg.getId());
        if(isOffline(service)) {
            return whenNotFoundService(arg.getId());
        }
        return service.stopContainer(arg);
    }

    /**
     * Restart specified by id container
     *
     * @param arg
     * @return
     */
    @Override
    public ServiceCallResult restartContainer(StopContainerArg arg) {
        DockerService service = getServiceByContainer(arg.getId());
        if(isOffline(service)) {
            return whenNotFoundService(arg.getId());
        }
        return service.restartContainer(arg);
    }

    /**
     * Get container logs
     *
     * @param arg
     * @return
     */
    @Override
    public ServiceCallResult getContainerLog(GetLogContainerArg arg) {
        DockerService service = getServiceByContainer(arg.getId());
        if(isOffline(service)) {
            return whenNotFoundService(arg.getId());
        }
        return service.getContainerLog(arg);
    }

    /**
     * Kill container
     *
     * @param arg
     * @return
     */
    @Override
    public ServiceCallResult killContainer(KillContainerArg arg) {
        DockerService service = getServiceByContainer(arg.getId());
        if(isOffline(service)) {
            return whenNotFoundService(arg.getId());
        }
        return service.killContainer(arg);
    }

    /**
     * Delete container
     *
     * @param arg
     * @return
     */
    @Override
    public ServiceCallResult deleteContainer(DeleteContainerArg arg) {
        DockerService service = getServiceByContainer(arg.getId());
        if(isOffline(service)) {
            return whenNotFoundService(arg.getId());
        }
        return service.deleteContainer(arg);
    }

    /**
     * Not supported, because we don't know which node in the group(ngroup) should create the container
     * and don't like nodes group united by swarm or docker in swarm mode can judge this by balance strategy
     * TODO in the feature we need to define our own strategy to create container in nodes group
     * @return not support
     */
    @Override
    public CreateContainerResponse createContainer(CreateContainerCmd cmd) {
        return notSupported(new CreateContainerResponse());
    }

    /**
     * Update container
     *
     * @param cmd
     * @return
     */
    @Override
    public ServiceCallResult updateContainer(UpdateContainerCmd cmd) {
        DockerService service = getServiceByContainer(cmd.getId());
        if (isOffline(service)) {
            return whenNotFoundService(cmd.getId());
        }
        return service.updateContainer(cmd);
    }

    /**
     * Rename Container
     *
     * @param id
     * @param newName
     * @return
     */
    @Override
    public ServiceCallResult renameContainer(String id, String newName) {
        DockerService service = getServiceByContainer(id);
        if (isOffline(service)) {
            return whenNotFoundService(id);
        }
        return service.renameContainer(id, newName);
    }

    /**
     * Not support, because we don't know which node in the group(ngroup) should subscribe the event
     * @@return  not support
     */
    @Override
    public ServiceCallResult subscribeToEvents(GetEventsArg arg) {
        return notSupport();
    }

    /**
     * Not support
     * @return not support result
     */
    @Override
    public CreateNetworkResponse createNetwork(CreateNetworkCmd cmd) {
        return notSupported(new CreateNetworkResponse());
    }

    /**
     * Not support yet
     * @throws UnsupportedOperationException
     */
    @Override
    public Network getNetwork(String id) {
        throw new UnsupportedOperationException("Virtual ngroup does not support.");
    }

    /**
     * Not support yet
     * @throws UnsupportedOperationException
     */
    @Override
    public List<Network> getNetworks() {
        throw new UnsupportedOperationException("Virtual ngroup does not support.");
    }

    /**
     * Not support
     * @return not support result
     */
    @Override
    public ServiceCallResult deleteNetwork(String id) {
        return notSupport();
    }

    /**
     * Not support
     * @return not support result
     */
    @Override
    public PruneNetworksResponse pruneNetworks(PruneNetworksArg arg) {
        PruneNetworksResponse res = new PruneNetworksResponse();
        return notSupported(res);
    }

    /**
     * Not support
     * @return not support result
     */
    @Override
    public ServiceCallResult connectNetwork(ConnectNetworkCmd cmd) {
        return notSupport();
    }

    /**
     * Not support
     * @return not support result
     */
    @Override
    public ServiceCallResult disconnectNetwork(DisconnectNetworkCmd cmd) {
        return notSupport();
    }

    /**
     * Get image
     *
     * @param arg
     * @return
     */
    @Override
    public List<ImageItem> getImages(GetImagesArg arg) {
        List<ImageItem> virt = new ArrayList<>();
        for(Node node: cluster.getNodes()) {
            // if node is null will throw exception here
            DockerService service = getServiceByNode(node);
            if(isOffline(service)) {
                // due to different causes service can be null
                continue;
            }
            try {
                List<ImageItem> images = service.getImages(arg);
                virt.addAll(images);
            } catch (AccessDeniedException e) {
                //nothing
            }
        }
        return virt;
    }

    /**
     * Not support
     * @throws UnsupportedOperationException
     */
    @Override
    public ImageDescriptor pullImage(String name, Consumer<ProcessEvent> watcher) {
        throw new UnsupportedOperationException("Virtual ngroup does not support.");
    }

    /**
     * return low-level information on the image name, not pull image.
     * Get the first image we found (more than one node has the image of same name in the ngroup)
     * @param name name with tag (otherwise retrieved the last image)
     * @return image of null when it not found
     */
    @Override
    public ImageDescriptor getImage(String name) {
        ImageDescriptor image = null;
        for(Node node: cluster.getNodes()) {
            DockerService service = getServiceByNode(node);
            if(isOffline(service)) {
                // due to different causes service can be null
                continue;
            }
            try {
                image = service.getImage(name);
                if (image != null) {
                    break;
                }
            } catch (AccessDeniedException e) {
                //nothing
            }
        }
        return image;
    }

    /**
     * Not support
     *
     * @return not support result
     */
    @Override
    public ServiceCallResult createTag(TagImageArg cmd) {
        return notSupport();
    }

    /**
     * Remove image
     *
     * @param arg
     * @return
     */
    @Override
    public RemoveImageResult removeImage(RemoveImageArg arg) {
        RemoveImageResult removeImageResult = new RemoveImageResult();
        removeImageResult.code(ResultCode.OK);
        for(Node node: cluster.getNodes()) {
            DockerService service = getServiceByNode(node);
            if (isOffline(service)) {
                continue;
            }
            try {
                service.removeImage(arg);
            } catch (AccessDeniedException e) {
                //nothing
            }
        }
        return removeImageResult;
    }

    /**
     * Just a virtual config, no sense
     *
     * @return
     */
    @Override
    public DockerConfig getDockerConfig() {
        return config;
    }

    /**
     * Not support
     * @throws UnsupportedOperationException
     */
    @Override
    public SwarmInspectResponse getSwarm() {
        throw new UnsupportedOperationException("Virtual ngroup does not support.");
    }

    /**
     * Not support
     * @throws UnsupportedOperationException
     */
    @Override
    public SwarmInitResult initSwarm(SwarmInitCmd cmd) {
        throw new UnsupportedOperationException("Virtual ngroup does not support.");
    }

    /**
     * Not support
     * @throws UnsupportedOperationException
     */
    @Override
    public ServiceCallResult joinSwarm(SwarmJoinCmd cmd) {
        throw new UnsupportedOperationException("Virtual ngroup does not support.");
    }

    /**
     * Not support
     * @throws UnsupportedOperationException
     */
    @Override
    public ServiceCallResult leaveSwarm(SwarmLeaveArg arg) {
        throw new UnsupportedOperationException("Virtual ngroup does not support.");
    }

    /**
     * Not support
     * @throws UnsupportedOperationException
     */
    @Override
    public List<SwarmNode> getNodes(GetNodesArg cmd) {
        throw new UnsupportedOperationException("Virtual ngroup does not support.");
    }

    /**
     * Not support
     *
     * @return not support result
     */
    @Override
    public ServiceCallResult removeNode(RemoveNodeArg arg) {
        return notSupport();
    }

    /**
     * Not support
     *
     * @return not support result
     */
    @Override
    public ServiceCallResult updateNode(UpdateNodeCmd cmd) {
        return notSupport();
    }

    /**
     * Not support
     * @throws UnsupportedOperationException
     */
    @Override
    public List<Service> getServices(GetServicesArg arg) {
        throw new UnsupportedOperationException("Virtual ngroup does not support.");
    }

    /**
     * Not support
     * @throws UnsupportedOperationException
     */
    @Override
    public ServiceCreateResult createService(CreateServiceArg arg) {
        throw new UnsupportedOperationException("Virtual ngroup does not support.");
    }

    /**
     * Not support
     *
     * @return not support result
     */
    @Override
    public ServiceUpdateResult updateService(UpdateServiceArg arg) {
        return notSupported(new ServiceUpdateResult());
    }

    /**
     * Not support
     *
     * @return not support result
     */
    @Override
    public ServiceCallResult deleteService(String service) {
        return notSupport();
    }

    /**
     * Not support
     * @throws UnsupportedOperationException
     */
    @Override
    public Service getService(String service) {
        throw new UnsupportedOperationException("Virtual ngroup does not support.");
    }

    /**
     * Not support
     * @throws UnsupportedOperationException
     */
    @Override
    public List<Task> getTasks(GetTasksArg arg) {
        throw new UnsupportedOperationException("Virtual ngroup does not support.");
    }

    /**
     * Not support
     * @throws UnsupportedOperationException
     */
    @Override
    public Task getTask(String taskId) {
        throw new UnsupportedOperationException("Virtual ngroup does not support.");
    }

    /**
     * Not support
     * @throws UnsupportedOperationException
     */
    @Override
    public List<Volume> getVolumes(GetVolumesArg arg) {
        throw new UnsupportedOperationException("Virtual ngroup does not support.");
    }

    /**
     * Not support
     * @throws UnsupportedOperationException
     */
    @Override
    public Volume getVolume(String name) {
        throw new UnsupportedOperationException("Virtual ngroup does not support.");
    }

    /**
     * Not support
     * @throws UnsupportedOperationException
     */
    @Override
    public Volume createVolume(CreateVolumeCmd cmd) {
        throw new UnsupportedOperationException("Virtual ngroup does not support.");
    }

    /**
     * Not support
     * @throws UnsupportedOperationException
     */
    @Override
    public ServiceCallResult removeVolume(RemoveVolumeArg arg) {
        throw new UnsupportedOperationException("Virtual ngroup does not support.");
    }

    /**
     * Not support
     *
     * @return not support result
     */
    @Override
    public ServiceCallResult deleteUnusedVolumes(DeleteUnusedVolumesArg arg) {
        return notSupport();
    }

    private final DefaultCluster cluster;
    // we need empty config for prevent NPE
    // just a virtual config because the actual docker service according to the specified node or specified container
    private final DockerConfig config = DockerConfig.builder().host("<virtual host>").build();

    public VirtualDockerService(DefaultCluster cluster) {
        this.cluster = cluster;
    }

    private ServiceCallResult notSupport() {
        ServiceCallResult res = new ServiceCallResult();
        return  notSupported(res);
    }

    private <T extends ServiceCallResult> T notSupported(T res) {
        res.code(ResultCode.ERROR).message("Virtual ngroup '" + getCluster() + "' does not support this.");
        return res;
    }

    private ServiceCallResult whenNotFoundService(String id) {
        return new ServiceCallResult().code(ResultCode.ERROR).message("Can not find service for container: " + id);
    }

    // get docker by container
    private DockerService getServiceByContainer(String id) {
        return NodeUtils.getDockerByContainer(cluster.getContainerStorage(), getNodeStorage(), id);
    }

    private DockerService getServiceByNode(Node node) {
        Assert.notNull(node, "Node is null");
        return getNodeStorage().getDockerService(node.getName());
    }

    private NodeStorage getNodeStorage() {
        return this.cluster.getNodeStorage();
    }
}
