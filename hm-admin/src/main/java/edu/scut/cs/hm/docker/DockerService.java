package edu.scut.cs.hm.docker;

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
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.function.Consumer;

/**
 * Docker client API
 */
public interface DockerService {

    /**
     * {@link ContainerDetails} cache name
     */
    String CACHE_CONTAINER_DETAILS = "ContainerDetails";
    /**
     * DockerService's id prefix
     */
    String DS_PREFIX = "ds:";

    /**
     * Name of ngroup
     * Note: only one of {@link #getCluster()} or {@link #getNode()} can has non null value
     * Because the docker service may represent docker in one node or in a ngroup
     * @return
     */
    String getCluster();

    /**
     * Name of node
     * Note: only one of {@link #getCluster()} or {@link #getNode()} can has non null value
     * Because the docker service may represent docker in one node or in a ngroup
     * @return
     */
    String getNode();

    /**
     * Return Id of Docker Service
     * @return
     */
    default String getId() {
        StringBuilder sb = new StringBuilder(DS_PREFIX);
        String cluster = getCluster();
        if (cluster != null) {
            sb.append("ngroup:").append(cluster);
        } else {
            sb.append("node:").append(getNode());
        }
        return sb.toString();
    }

    /**
     * Address of docker service in 'ip:port' format, sometime it can be null.
     * @return address or null
     */
    String getAddress();

    /**
     * Docker service is online or not
     * @return
     */
    boolean isOnline();

    /**
     * Retrieve details info about one container.
     * @param id
     * @return container details or null if not found
     */
    @Cacheable(CACHE_CONTAINER_DETAILS)
    ContainerDetails getContainer(String id);

    /**
     * Retrieve list of Docker containers
     * @param arg
     * @return
     */
    List<DockerContainer> getContainers(GetContainersArg arg);

    /**
     * Get container stats based on resource usage
     * @param arg
     * @return
     */
    ServiceCallResult getStatistics(GetStatisticsArg arg);

    /**
     * Display system-wide information
     * @return info
     */
    DockerServiceInfo getInfo();

    /**
     * Start specified by id container
     * @param id id of container
     * @return result
     */
    ServiceCallResult startContainer(String id);

    /**
     * Pause specified by id container
     * @param id id of container
     * @return result
     */
    ServiceCallResult pauseContainer(String id);

    /**
     * Run previously paused container
     * @param id id of container
     * @return result
     */
    ServiceCallResult unpauseContainer(String id);

    /**
     * Stop specified by id container
     * @param arg
     * @return result
     */
    ServiceCallResult stopContainer(StopContainerArg arg);

    /**
     * Restart specified by id container
     * @param arg
     * @return
     */
    ServiceCallResult restartContainer(StopContainerArg arg);

    /**
     * Get container logs
     * @param arg
     * @return
     */
    ServiceCallResult getContainerLog(GetLogContainerArg arg);

    /**
     * Kill container
     * @param arg
     * @return
     */
    ServiceCallResult killContainer(KillContainerArg arg);

    /**
     * Delete container
     * @param arg
     * @return
     */
    ServiceCallResult deleteContainer(DeleteContainerArg arg);

    /**
     * Create container
     * @param cmd
     * @return
     */
    CreateContainerResponse createContainer(CreateContainerCmd cmd);

    /**
     * Update container
     * @param cmd
     * @return
     */
    ServiceCallResult updateContainer(UpdateContainerCmd cmd);

    /**
     * Rename Container
     * @param id
     * @param newName
     * @return
     */
    ServiceCallResult renameContainer(String id, String newName);

    /**
     * Subscribe a watcher in GetEventsArg to Docker event api
     * @param arg
     * @return
     */
    ServiceCallResult subscribeToEvents(GetEventsArg arg);

    /**
     * Create network
     * @param cmd
     * @return
     */
    CreateNetworkResponse createNetwork(CreateNetworkCmd cmd);

    /**
     * Get network by id
     * @param id
     * @return
     */
    Network getNetwork(String id);

    /**
     * Retrieve networks
     * @return
     */
    List<Network> getNetworks();

    /**
     * Delete network by id
     * @param id
     * @return
     */
    ServiceCallResult deleteNetwork(String id);

    /**
     * Delete unused networks
     * @param arg arg with filter for networks
     * @return result with list of deleted networks
     */
    PruneNetworksResponse pruneNetworks(PruneNetworksArg arg);

    /**
     * Connect specified container to network
     * @param cmd command
     * @return result
     */
    ServiceCallResult connectNetwork(ConnectNetworkCmd cmd);

    /**
     * Disconnect specified container from network.
     * @param cmd command
     * @return result
     */
    ServiceCallResult disconnectNetwork(DisconnectNetworkCmd cmd);

    /**
     * Get image
     * @param arg
     * @return
     */
    List<ImageItem> getImages(GetImagesArg arg);

    /**
     * Pull image and return low-level information on the image name
     * @param name name with tag (otherwise retrieved the last image)
     * @param watcher consume events in method execution, allow null
     * @return image
     */
    @Cacheable(value = "Image", key = "name")
    ImageDescriptor pullImage(String name, Consumer<ProcessEvent> watcher);

    /**
     * return low-level information on the image name, not pull image.
     * @param name name with tag (otherwise retrieved the last image)
     * @return image of null when it not found
     */
    ImageDescriptor getImage(String name);

    /**
     * Create image tag
     * @param cmd
     * @return
     */
    ServiceCallResult createTag(TagImageArg cmd);

    /**
     * Remove image
     * @param arg
     * @return
     */
    RemoveImageResult removeImage(RemoveImageArg arg);

    /**
     * May return null when is not support
     * @return
     */
    DockerConfig getDockerConfig();

    /**
     * Inspect swarm.
     * <code>GET /swarm</code>
     * @return swarm config or null when not supported
     */
    InspectResponse getSwarm();

    /**
     * Initialize a new swarm. The body of the HTTP response includes the node ID.
     * <code>POST /swarm/init</code>
     * @param cmd command to init swarm
     * @return result with node id or null when not supported
     */
    SwarmInitResult initSwarm(SwarmInitCmd cmd);

    /**
     * Join into existing swarm. <p/>
     * <code>POST /swarm/join</code>
     * @param cmd command args
     * @return result code
     */
    ServiceCallResult joinSwarm(SwarmJoinCmd cmd);

    /**
     * Leave existing swarm
     * @param arg
     * @return
     */
    ServiceCallResult leaveSwarm(SwarmLeaveArg arg);

    /**
     * Get list of nodes. Work only for docker in swarm-mode.
     * @param cmd pass arg with filters or null
     * @return list or null when not supported
     */
    List<SwarmNode> getNodes(GetNodesArg cmd);

    /**
     * Remove node. Work only for docker in swarm-mode.
     * @param arg
     * @return
     */
    ServiceCallResult removeNode(RemoveNodeArg arg);

    /**
     * update node. Work only for docker in swarm-mode.
     * @param cmd
     * @return
     */
    ServiceCallResult updateNode(UpdateNodeCmd cmd);

    /**
     * Get service (concept in docker swarm mode)
     * @param arg
     * @return
     */
    List<Service> getServices(GetServicesArg arg);

    /**
     * Create service
     * @param arg
     * @return
     */
    ServiceCreateResult createService(CreateServiceArg arg);

    /**
     * Update a service
     * POST /services/(id or name)/update
     * @param arg argument
     * @return result of scale ops
     */
    ServiceUpdateResult updateService(UpdateServiceArg arg);

    /**
     * DELETE /services/(id or name)
     * @param service id or name
     * @return result
     */
    ServiceCallResult deleteService(String service);

    /**
     * GET /services/(id or name)
     * Return information on the service id.
     * @param service id or name
     * @return service or null
     */
    Service getService(String service);

    /**
     * List Task (concept in docker swarm mode)
     * @param arg
     * @return
     */
    List<Task> getTasks(GetTasksArg arg);

    /**
     * Get Task (concept in docker swarm mode)
     * @param taskId
     * @return
     */
    Task getTask(String taskId);

    /**
     * List volumes
     * @param arg
     * @return
     */
    List<Volume> getVolumes(GetVolumesArg arg);

    /**
     *
     * @param name Volume name or ID
     * @return volume or null
     */
    Volume getVolume(String name);

    /**
     * Create volume
     * @param cmd
     * @return
     */
    Volume createVolume(CreateVolumeCmd cmd);

    /**
     * Remove volume
     * @param arg
     * @return
     */
    ServiceCallResult removeVolume(RemoveVolumeArg arg);

    /**
     * Remove unused volumes
     * @param arg
     * @return
     */
    ServiceCallResult deleteUnusedVolumes(DeleteUnusedVolumesArg arg);
}
