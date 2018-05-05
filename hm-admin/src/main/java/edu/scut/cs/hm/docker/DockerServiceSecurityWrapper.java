package edu.scut.cs.hm.docker;

import edu.scut.cs.hm.admin.security.AccessContext;
import edu.scut.cs.hm.admin.security.AccessContextFactory;
import edu.scut.cs.hm.admin.security.SecuredType;
import edu.scut.cs.hm.common.security.acl.dto.Action;
import edu.scut.cs.hm.docker.arg.*;
import edu.scut.cs.hm.docker.cmd.*;
import edu.scut.cs.hm.docker.model.container.ContainerDetails;
import edu.scut.cs.hm.docker.model.container.DockerContainer;
import edu.scut.cs.hm.docker.model.image.ImageDescriptor;
import edu.scut.cs.hm.docker.model.image.ImageItem;
import edu.scut.cs.hm.docker.model.network.Network;
import edu.scut.cs.hm.docker.model.swarm.Service;
import edu.scut.cs.hm.docker.model.swarm.SwarmNode;
import edu.scut.cs.hm.docker.model.swarm.Task;
import edu.scut.cs.hm.docker.model.volume.Volume;
import edu.scut.cs.hm.docker.res.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.Assert;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Wrap DockerService with ACL Security
 */
public class DockerServiceSecurityWrapper implements DockerService {
    private final AccessContextFactory aclContextFactory;
    private final DockerService service;

    public DockerServiceSecurityWrapper(AccessContextFactory aclContextFactory, DockerService service) {
        this.aclContextFactory = aclContextFactory;
        this.service = service;
    }

    public void checkServiceAccess(Action action) {
        AccessContext context = aclContextFactory.getContext();
        checkServiceAccessInternal(context, action);
    }

    private void checkServiceAccessInternal(AccessContext context, Action action) {
        checkClusterAccess(context, action);
        String node = getNode();
        if(node != null) {
            boolean granted = context.isGranted(SecuredType.NODE.id(node), action);
            if(!granted) {
                throw new AccessDeniedException("Access to node docker service '" + node + "' with " + action + " is denied.");
            }
        }
    }

    private void checkClusterAccess(AccessContext context, Action action) {
        Assert.notNull(action, "Action is null");
        String cluster = getCluster();
        if(cluster != null) {
            boolean granted = context.isGranted(SecuredType.CLUSTER.id(cluster), action);
            if(!granted) {
                throw new AccessDeniedException("Access to ngroup docker service '" + cluster + "' with " + action + " is denied.");
            }
        }
    }

    public void checkContainerAccess(String id, Action action) {
        Assert.notNull(action, "Action is null");
        AccessContext context = aclContextFactory.getContext();
        checkServiceAccessInternal(context, action == Action.READ? Action.READ : Action.ALTER_INSIDE);
        boolean granted = context.isGranted(SecuredType.CONTAINER.id(id), action);
        if(!granted) {
            throw new AccessDeniedException("Access to container '" + id + "' with " + action + " is denied.");
        }
    }

    public void checkImageAccess(AccessContext context, String id, Action action) {
        Assert.notNull(action, "Action is null");
        checkServiceAccessInternal(context, action == Action.READ? Action.READ : Action.ALTER_INSIDE);
        boolean granted = context.isGranted(SecuredType.LOCAL_IMAGE.id(id), action);
        if(!granted) {
            throw new AccessDeniedException("Access to image '" + id + "' with " + action + " is denied.");
        }
    }

    public void checkNetworkAccess(String name, Action action) {
        Assert.notNull(action, "Action is null");
        AccessContext context = aclContextFactory.getContext();
        checkServiceAccessInternal(context, action == Action.READ? Action.READ : Action.ALTER_INSIDE);
        boolean granted = context.isGranted(SecuredType.NETWORK.id(name), action);
        if(!granted) {
            throw new AccessDeniedException("Access to image '" + name + "' with " + action + " is denied.");
        }
    }

    @Override
    public String getCluster() {
        return service.getCluster();
    }

    @Override
    public String getNode() {
        return service.getNode();
    }

    @Override
    public String getAddress() {
        return service.getAddress();
    }

    @Override
    public boolean isOnline() {
        return service.isOnline();
    }

    /**
     * Retrieve details info about one container.
     *
     * @param id
     * @return container details or null if not found
     */
    @Override
    public ContainerDetails getContainer(String id) {
        checkContainerAccess(id, Action.READ);
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
        AccessContext context = aclContextFactory.getContext();
        checkServiceAccessInternal(context, Action.READ);
        return service.getContainers(arg).stream().filter((img) -> {
            return context.isGranted(SecuredType.CONTAINER.id(img.getId()), Action.READ);
        }).collect(Collectors.toList());
    }

    /**
     * Get container stats based on resource usage
     *
     * @param arg
     * @return
     */
    @Override
    public ServiceCallResult getStatistics(GetStatisticsArg arg) {
        checkContainerAccess(arg.getId(), Action.READ);
        return service.getStatistics(arg);
    }

    /**
     * Display system-wide information
     *
     * @return info
     */
    @Override
    public DockerServiceInfo getInfo() {
        checkServiceAccess(Action.READ);
        return service.getInfo();
    }

    /**
     * Start specified by id container
     *
     * @param id id of container
     * @return result
     */
    @Override
    public ServiceCallResult startContainer(String id) {
        checkContainerAccess(id, Action.EXECUTE);
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
        checkContainerAccess(id, Action.EXECUTE);
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
        checkContainerAccess(id, Action.EXECUTE);
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
        checkContainerAccess(arg.getId(), Action.EXECUTE);
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
        checkContainerAccess(arg.getId(), Action.EXECUTE);
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
        checkContainerAccess(arg.getId(), Action.READ);
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
        checkContainerAccess(arg.getId(), Action.DELETE);
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
        checkContainerAccess(arg.getId(), Action.DELETE);
        return service.deleteContainer(arg);
    }

    /**
     * Create container
     *
     * @param cmd
     * @return
     */
    @Override
    public CreateContainerResponse createContainer(CreateContainerCmd cmd) {
        checkContainerAccess(null, Action.CREATE);
        return service.createContainer(cmd);
    }

    /**
     * Update container
     *
     * @param cmd
     * @return
     */
    @Override
    public ServiceCallResult updateContainer(UpdateContainerCmd cmd) {
        checkContainerAccess(cmd.getId(), Action.UPDATE);
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
        checkContainerAccess(id, Action.UPDATE);
        return service.renameContainer(id, newName);
    }

    @Override
    public ServiceCallResult subscribeToEvents(GetEventsArg arg) {
        checkServiceAccess(Action.READ);
        return service.subscribeToEvents(arg);
    }

    /**
     * Create network
     *
     * @param cmd
     * @return
     */
    @Override
    public CreateNetworkResponse createNetwork(CreateNetworkCmd cmd) {
        checkNetworkAccess(cmd.getName(), Action.CREATE);
        return service.createNetwork(cmd);
    }

    /**
     * Get network by id
     *
     * @param id
     * @return
     */
    @Override
    public Network getNetwork(String id) {
        checkNetworkAccess(id, Action.READ);
        return service.getNetwork(id);
    }

    @Override
    public List<Network> getNetworks() {
        AccessContext context = aclContextFactory.getContext();
        checkServiceAccessInternal(context, Action.READ);
        return service.getNetworks().stream().filter((net) -> context.isGranted(SecuredType.NETWORK.id(net.getId()), Action.READ))
                .collect(Collectors.toList());
    }

    /**
     * Delete network by id
     *
     * @param id
     * @return
     */
    @Override
    public ServiceCallResult deleteNetwork(String id) {
        checkNetworkAccess(id, Action.DELETE);
        return service.deleteNetwork(id);
    }

    /**
     * Delete unused networks
     *
     * @param arg arg with filter for networks
     * @return result with list of deleted networks
     */
    @Override
    public PruneNetworksResponse pruneNetworks(PruneNetworksArg arg) {
        checkServiceAccess(Action.ALTER_INSIDE);
        return service.pruneNetworks(arg);
    }

    /**
     * Connect specified container to network
     *
     * @param cmd command
     * @return result
     */
    @Override
    public ServiceCallResult connectNetwork(ConnectNetworkCmd cmd) {
        checkNetworkAccess(cmd.getNetwork(), Action.UPDATE);
        checkContainerAccess(cmd.getContainer(), Action.UPDATE);
        return service.connectNetwork(cmd);
    }

    /**
     * Disconnect specified container from network.
     *
     * @param cmd command
     * @return result
     */
    @Override
    public ServiceCallResult disconnectNetwork(DisconnectNetworkCmd cmd) {
        checkNetworkAccess(cmd.getNetwork(), Action.UPDATE);
        checkContainerAccess(cmd.getContainer(), Action.UPDATE);
        return service.disconnectNetwork(cmd);
    }

    /**
     * Get image
     *
     * @param arg
     * @return
     */
    @Override
    public List<ImageItem> getImages(GetImagesArg arg) {
        AccessContext context = aclContextFactory.getContext();
        checkServiceAccessInternal(context, Action.READ);
        return  service.getImages(arg).stream().filter((img) -> {
            return context.isGranted(SecuredType.LOCAL_IMAGE.id(img.getId()), Action.READ);
        }).collect(Collectors.toList());
    }

    /**
     * Pull image and return low-level information on the image name
     *
     * @param name    name with tag (otherwise retrieved the last image)
     * @param watcher consume events in method execution, allow null
     * @return image
     */
    @Override
    public ImageDescriptor pullImage(String name, Consumer<ProcessEvent> watcher) {
        // here service can load image, but we cannot check access by name, and need check it by id after loading
        AccessContext context = aclContextFactory.getContext();
        checkServiceAccessInternal(context, Action.READ);
        ImageDescriptor image = service.pullImage(name, watcher);
        checkImageAccess(context, name, Action.READ);
        return image;
    }

    /**
     * return low-level information on the image name, not pull image.
     *
     * @param name name with tag (otherwise retrieved the last image)
     * @return image of null when it not found
     */
    @Override
    public ImageDescriptor getImage(String name) {
        // here service can load image, but we cannot check access by name, and need check it by id after loading
        AccessContext context = aclContextFactory.getContext();
        checkServiceAccessInternal(context, Action.READ);
        ImageDescriptor image = service.getImage(name);
        checkImageAccess(context, name, Action.READ);
        return image;
    }

    /**
     * Create image tag
     *
     * @param cmd
     * @return
     */
    @Override
    public ServiceCallResult createTag(TagImageArg cmd) {
        checkImageAccess(aclContextFactory.getContext(), cmd.getImageName(), Action.UPDATE);
        return service.createTag(cmd);
    }

    /**
     * Remove image
     *
     * @param arg
     * @return
     */
    @Override
    public RemoveImageResult removeImage(RemoveImageArg arg) {
        checkImageAccess(aclContextFactory.getContext(), arg.getImageId(), Action.DELETE);
        return service.removeImage(arg);
    }

    /**
     * May return null when is not support
     *
     * @return
     */
    @Override
    public DockerConfig getDockerConfig() {
        checkServiceAccess(Action.READ);
        return service.getDockerConfig();
    }

    /**
     * Inspect swarm.
     * <code>GET /swarm</code>
     *
     * @return swarm config or null when not supported
     */
    @Override
    public SwarmInspectResponse getSwarm() {
        checkServiceAccess(Action.READ);
        return service.getSwarm();
    }

    /**
     * Initialize a new swarm. The body of the HTTP response includes the node ID.
     * <code>POST /swarm/init</code>
     *
     * @param cmd command to init swarm
     * @return result with node id or null when not supported
     */
    @Override
    public SwarmInitResult initSwarm(SwarmInitCmd cmd) {
        checkServiceAccess(Action.UPDATE);
        return service.initSwarm(cmd);
    }

    /**
     * Join into existing swarm. <p/>
     * <code>POST /swarm/join</code>
     *
     * @param cmd command args
     * @return result code
     */
    @Override
    public ServiceCallResult joinSwarm(SwarmJoinCmd cmd) {
        checkServiceAccess(Action.UPDATE);
        return service.joinSwarm(cmd);
    }

    /**
     * Leave existing swarm
     *
     * @param arg
     * @return
     */
    @Override
    public ServiceCallResult leaveSwarm(SwarmLeaveArg arg) {
        checkServiceAccess(Action.UPDATE);
        return service.leaveSwarm(arg);
    }

    /**
     * Get list of nodes. Work only for docker in swarm-mode.
     *
     * @param cmd pass arg with filters or null
     * @return list or null when not supported
     */
    @Override
    public List<SwarmNode> getNodes(GetNodesArg cmd) {
        checkServiceAccess(Action.READ);
        return service.getNodes(cmd);
    }

    /**
     * Remove node. Work only for docker in swarm-mode.
     *
     * @param arg
     * @return
     */
    @Override
    public ServiceCallResult removeNode(RemoveNodeArg arg) {
        checkServiceAccess(Action.ALTER_INSIDE);
        return service.removeNode(arg);
    }

    /**
     * update node. Work only for docker in swarm-mode.
     *
     * @param cmd
     * @return
     */
    @Override
    public ServiceCallResult updateNode(UpdateNodeCmd cmd) {
        checkServiceAccess(Action.ALTER_INSIDE);
        return service.updateNode(cmd);
    }

    /**
     * Get service (concept in docker swarm mode)
     *
     * @param arg
     * @return
     */
    @Override
    public List<Service> getServices(GetServicesArg arg) {
        checkServiceAccess(Action.READ);
        return this.service.getServices(arg);
    }

    /**
     * Create service
     *
     * @param arg
     * @return
     */
    @Override
    public ServiceCreateResult createService(CreateServiceArg arg) {
        checkServiceAccess(Action.ALTER_INSIDE);
        return service.createService(arg);
    }

    /**
     * Update a service
     * POST /services/(id or name)/update
     *
     * @param arg argument
     * @return result of scale ops
     */
    @Override
    public ServiceUpdateResult updateService(UpdateServiceArg arg) {
        checkServiceAccess(Action.ALTER_INSIDE);
        return service.updateService(arg);
    }

    /**
     * DELETE /services/(id or name)
     *
     * @param service id or name
     * @return result
     */
    @Override
    public ServiceCallResult deleteService(String service) {
        checkServiceAccess(Action.ALTER_INSIDE);
        return this.service.deleteService(service);
    }

    /**
     * GET /services/(id or name)
     * Return information on the service id.
     *
     * @param service id or name
     * @return service or null
     */
    @Override
    public Service getService(String service) {
        checkServiceAccess(Action.READ);
        return this.service.getService(service);
    }

    /**
     * List Task (concept in docker swarm mode)
     *
     * @param arg
     * @return
     */
    @Override
    public List<Task> getTasks(GetTasksArg arg) {
        checkServiceAccess(Action.READ);
        return service.getTasks(arg);
    }

    /**
     * Get Task (concept in docker swarm mode)
     *
     * @param taskId
     * @return
     */
    @Override
    public Task getTask(String taskId) {
        checkServiceAccess(Action.READ);
        return service.getTask(taskId);
    }

    /**
     * List volumes
     *
     * @param arg
     * @return
     */
    @Override
    public List<Volume> getVolumes(GetVolumesArg arg) {
        checkServiceAccess(Action.READ);
        return service.getVolumes(arg);
    }

    /**
     * @param name Volume name or ID
     * @return volume or null
     */
    @Override
    public Volume getVolume(String name) {
        checkServiceAccess(Action.READ);
        return service.getVolume(name);
    }

    /**
     * Create volume
     *
     * @param cmd
     * @return
     */
    @Override
    public Volume createVolume(CreateVolumeCmd cmd) {
        checkServiceAccess(Action.ALTER_INSIDE);
        return service.createVolume(cmd);
    }

    /**
     * Remove volume
     *
     * @param arg
     * @return
     */
    @Override
    public ServiceCallResult removeVolume(RemoveVolumeArg arg) {
        checkServiceAccess(Action.ALTER_INSIDE);
        return service.removeVolume(arg);
    }

    /**
     * Remove unused volumes
     *
     * @param arg
     * @return
     */
    @Override
    public ServiceCallResult deleteUnusedVolumes(DeleteUnusedVolumesArg arg) {
        checkServiceAccess(Action.ALTER_INSIDE);
        return service.deleteUnusedVolumes(arg);
    }
}
