package edu.scut.cs.hm.docker;

import edu.scut.cs.hm.admin.service.NodeService;
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
import edu.scut.cs.hm.model.cluster.DefaultNodesGroupImpl;
import edu.scut.cs.hm.model.node.NodeUtils;

import java.util.List;
import java.util.function.Consumer;

/**
 * Docker service for virtual nodes group (not united by docker in swarm mode or swarm)
 */
public class VirtualDockerService implements DockerService {

    public static boolean isOffline(DockerService service) {
        return service == null || !service.isOnline();
    }

    /**
     * Get the Name of cluster {@link DefaultNodesGroupImpl}
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
     * @return container details or null if not found
     */
    @Override
    public ContainerDetails getContainer(String id) {
        return null;
    }

    private DockerService getServiceByContainer(String id) {
        return NodeUtils.getDockerByContainer(cluster.getContainerService(), getNodeService(), id);
    }

    /**
     * Retrieve list of Docker containers
     *
     * @param arg
     * @return
     */
    @Override
    public List<DockerContainer> getContainers(GetContainersArg arg) {
        return null;
    }

    /**
     * Get container stats based on resource usage
     *
     * @param arg
     * @return
     */
    @Override
    public ServiceCallResult getStatistics(GetStatisticsArg arg) {
        return null;
    }

    /**
     * Display system-wide information
     *
     * @return info
     */
    @Override
    public DockerServiceInfo getInfo() {
        return null;
    }

    /**
     * Start specified by id container
     *
     * @param id id of container
     * @return result
     */
    @Override
    public ServiceCallResult startContainer(String id) {
        return null;
    }

    /**
     * Pause specified by id container
     *
     * @param id id of container
     * @return result
     */
    @Override
    public ServiceCallResult pauseContainer(String id) {
        return null;
    }

    /**
     * Run previously paused container
     *
     * @param id id of container
     * @return result
     */
    @Override
    public ServiceCallResult unpauseContainer(String id) {
        return null;
    }

    /**
     * Stop specified by id container
     *
     * @param arg
     * @return result
     */
    @Override
    public ServiceCallResult stopContainer(StopContainerArg arg) {
        return null;
    }

    /**
     * Restart specified by id container
     *
     * @param arg
     * @return
     */
    @Override
    public ServiceCallResult restartContainer(StopContainerArg arg) {
        return null;
    }

    /**
     * Get container logs
     *
     * @param arg
     * @return
     */
    @Override
    public ServiceCallResult getContainerLog(GetLogContainerArg arg) {
        return null;
    }

    /**
     * Kill container
     *
     * @param arg
     * @return
     */
    @Override
    public ServiceCallResult killContainer(KillContainerArg arg) {
        return null;
    }

    /**
     * Delete container
     *
     * @param arg
     * @return
     */
    @Override
    public ServiceCallResult deleteContainer(DeleteContainerArg arg) {
        return null;
    }

    /**
     * Create container
     *
     * @param cmd
     * @return
     */
    @Override
    public CreateContainerResponse createContainer(CreateContainerCmd cmd) {
        return null;
    }

    /**
     * Update container
     *
     * @param cmd
     * @return
     */
    @Override
    public ServiceCallResult updateContainer(UpdateContainerCmd cmd) {
        return null;
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
        return null;
    }

    /**
     * Subscribe a watcher in GetEventsArg to Docker event api
     *
     * @param arg
     * @return
     */
    @Override
    public ServiceCallResult subscribeToEvents(GetEventsArg arg) {
        return null;
    }

    /**
     * Create network
     *
     * @param cmd
     * @return
     */
    @Override
    public CreateNetworkResponse createNetwork(CreateNetworkCmd cmd) {
        return null;
    }

    /**
     * Get network by id
     *
     * @param id
     * @return
     */
    @Override
    public Network getNetwork(String id) {
        return null;
    }

    /**
     * Retrieve networks
     *
     * @return
     */
    @Override
    public List<Network> getNetworks() {
        return null;
    }

    /**
     * Delete network by id
     *
     * @param id
     * @return
     */
    @Override
    public ServiceCallResult deleteNetwork(String id) {
        return null;
    }

    /**
     * Delete unused networks
     *
     * @param arg arg with filter for networks
     * @return result with list of deleted networks
     */
    @Override
    public PruneNetworksResponse pruneNetworks(PruneNetworksArg arg) {
        return null;
    }

    /**
     * Connect specified container to network
     *
     * @param cmd command
     * @return result
     */
    @Override
    public ServiceCallResult connectNetwork(ConnectNetworkCmd cmd) {
        return null;
    }

    /**
     * Disconnect specified container from network.
     *
     * @param cmd command
     * @return result
     */
    @Override
    public ServiceCallResult disconnectNetwork(DisconnectNetworkCmd cmd) {
        return null;
    }

    /**
     * Get image
     *
     * @param arg
     * @return
     */
    @Override
    public List<ImageItem> getImages(GetImagesArg arg) {
        return null;
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
        return null;
    }

    /**
     * return low-level information on the image name, not pull image.
     *
     * @param name name with tag (otherwise retrieved the last image)
     * @return image of null when it not found
     */
    @Override
    public ImageDescriptor getImage(String name) {
        return null;
    }

    /**
     * Create image tag
     *
     * @param cmd
     * @return
     */
    @Override
    public ServiceCallResult createTag(TagImageArg cmd) {
        return null;
    }

    /**
     * Remove image
     *
     * @param arg
     * @return
     */
    @Override
    public RemoveImageResult removeImage(RemoveImageArg arg) {
        return null;
    }

    /**
     * May return null when is not support
     *
     * @return
     */
    @Override
    public DockerConfig getDockerConfig() {
        return null;
    }

    /**
     * Inspect swarm.
     * <code>GET /swarm</code>
     *
     * @return swarm config or null when not supported
     */
    @Override
    public InspectResponse getSwarm() {
        return null;
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
        return null;
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
        return null;
    }

    /**
     * Leave existing swarm
     *
     * @param arg
     * @return
     */
    @Override
    public ServiceCallResult leaveSwarm(SwarmLeaveArg arg) {
        return null;
    }

    /**
     * Get list of nodes. Work only for docker in swarm-mode.
     *
     * @param cmd pass arg with filters or null
     * @return list or null when not supported
     */
    @Override
    public List<SwarmNode> getNodes(GetNodesArg cmd) {
        return null;
    }

    /**
     * Remove node. Work only for docker in swarm-mode.
     *
     * @param arg
     * @return
     */
    @Override
    public ServiceCallResult removeNode(RemoveNodeArg arg) {
        return null;
    }

    /**
     * update node. Work only for docker in swarm-mode.
     *
     * @param cmd
     * @return
     */
    @Override
    public ServiceCallResult updateNode(UpdateNodeCmd cmd) {
        return null;
    }

    /**
     * Get service (concept in docker swarm mode)
     *
     * @param arg
     * @return
     */
    @Override
    public List<Service> getServices(GetServicesArg arg) {
        return null;
    }

    /**
     * Create service
     *
     * @param arg
     * @return
     */
    @Override
    public ServiceCreateResult createService(CreateServiceArg arg) {
        return null;
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
        return null;
    }

    /**
     * DELETE /services/(id or name)
     *
     * @param service id or name
     * @return result
     */
    @Override
    public ServiceCallResult deleteService(String service) {
        return null;
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
        return null;
    }

    /**
     * List Task (concept in docker swarm mode)
     *
     * @param arg
     * @return
     */
    @Override
    public List<Task> getTasks(GetTasksArg arg) {
        return null;
    }

    /**
     * Get Task (concept in docker swarm mode)
     *
     * @param taskId
     * @return
     */
    @Override
    public Task getTask(String taskId) {
        return null;
    }

    /**
     * List volumes
     *
     * @param arg
     * @return
     */
    @Override
    public List<Volume> getVolumes(GetVolumesArg arg) {
        return null;
    }

    /**
     * @param name Volume name or ID
     * @return volume or null
     */
    @Override
    public Volume getVolume(String name) {
        return null;
    }

    /**
     * Create volume
     *
     * @param cmd
     * @return
     */
    @Override
    public Volume createVolume(CreateVolumeCmd cmd) {
        return null;
    }

    /**
     * Remove volume
     *
     * @param arg
     * @return
     */
    @Override
    public ServiceCallResult removeVolume(RemoveVolumeArg arg) {
        return null;
    }

    /**
     * Remove unused volumes
     *
     * @param arg
     * @return
     */
    @Override
    public ServiceCallResult deleteUnusedVolumes(DeleteUnusedVolumesArg arg) {
        return null;
    }

    private final DefaultNodesGroupImpl cluster;
    private final DockerConfig config = DockerConfig.builder().host("<virtual host>").build();  // we need empty config for prevent NPE

    public VirtualDockerService(DefaultNodesGroupImpl cluster) {
        this.cluster = cluster;
    }

    private ServiceCallResult notSupport() {
        ServiceCallResult res = new ServiceCallResult();
        return  notSupported(res);
    }

    private <T extends ServiceCallResult> T notSupported(T res) {
        res.code(ResultCode.ERROR).message("Virtual cluster '" + getCluster() + "' does not support this.");
        return res;
    }

    private NodeService getNodeService() {
        return this.cluster.getNodeService();
    }
}
