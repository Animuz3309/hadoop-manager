package edu.scut.cs.hm.docker;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.scut.cs.hm.docker.arg.*;
import edu.scut.cs.hm.docker.cmd.*;
import edu.scut.cs.hm.docker.model.container.ContainerDetails;
import edu.scut.cs.hm.docker.model.container.DockerContainer;
import edu.scut.cs.hm.docker.model.events.DockerServiceEvent;
import edu.scut.cs.hm.docker.model.image.ImageDescriptor;
import edu.scut.cs.hm.docker.model.image.ImageItem;
import edu.scut.cs.hm.docker.model.network.Network;
import edu.scut.cs.hm.docker.model.swarm.Service;
import edu.scut.cs.hm.docker.model.swarm.SwarmNode;
import edu.scut.cs.hm.docker.model.swarm.Task;
import edu.scut.cs.hm.docker.model.volume.Volume;
import edu.scut.cs.hm.docker.res.*;
import edu.scut.cs.hm.model.node.NodeInfoProvider;
import lombok.Data;
import org.springframework.util.Assert;
import org.springframework.web.client.AsyncRestTemplate;

import java.util.List;
import java.util.function.Consumer;

/**
 * Docker Service implementation
 * TODO finish docker service api
 */
public class DockerServiceImpl implements DockerService {

    /**
     * Name of ngroup
     * Note: only one of {@link #getCluster()} or {@link #getNode()} can has non null value
     *
     * @return
     */
    @Override
    public String getCluster() {
        return null;
    }

    /**
     * Name of node
     * ote: only one of {@link #getCluster()} or {@link #getNode()} can has non null value
     *
     * @return
     */
    @Override
    public String getNode() {
        return null;
    }

    /**
     * Return Id of Docker Service
     *
     * @return
     */
    @Override
    public String getId() {
        return null;
    }

    /**
     * Address of docker service in 'ip:port' format, sometime it can be null.
     *
     * @return address or null
     */
    @Override
    public String getAddress() {
        return null;
    }

    /**
     * Docker service is online or not
     *
     * @return
     */
    @Override
    public boolean isOnline() {
        return false;
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

    @Data
    public static class Builder {
        private String node;
        private String cluster;
        private DockerConfig config;
        @SuppressWarnings("deprecation")
        private AsyncRestTemplate restTemplate;
        private NodeInfoProvider nodeInfoProvider;
        private Consumer<DockerServiceEvent> eventConsumer;
        /**
         * At this interceptor you may modify building of {@link DockerServiceInfo}
         */
        private Consumer<DockerServiceInfo.Builder> infoInterceptor;
        private ObjectMapper objectMapper;

        public Builder node(String node) {
            setNode(node);
            return this;
        }

        public Builder cluster(String cluster) {
            setCluster(cluster);
            return this;
        }

        public Builder config(DockerConfig config) {
            setConfig(config);
            return this;
        }

        @SuppressWarnings("deprecation")
        public Builder restTemplate(AsyncRestTemplate restTemplate) {
            setRestTemplate(restTemplate);
            return this;
        }

        public Builder nodeInfoProvider(NodeInfoProvider nodeInfoProvider) {
            setNodeInfoProvider(nodeInfoProvider);
            return this;
        }

        public Builder eventConsumer(Consumer<DockerServiceEvent> dockerServiceBus) {
            setEventConsumer(dockerServiceBus);
            return this;
        }

        public Builder infoInterceptor(Consumer<DockerServiceInfo.Builder> infoInterceptor) {
            setInfoInterceptor(infoInterceptor);
            return this;
        }

        public Builder objectMapper(ObjectMapper objectMapper) {
            setObjectMapper(objectMapper);
            return this;
        }

        public DockerServiceImpl build() {
            return new DockerServiceImpl(this);
        }
    }

    private final String node;
    private final String cluster;
    private final DockerConfig config;
    @SuppressWarnings("deprecation")
    private final AsyncRestTemplate restTemplate;
    private final NodeInfoProvider nodeInfoProvider;
    private final Consumer<DockerServiceEvent> eventConsumer;
    private final Consumer<DockerServiceInfo.Builder> infoInterceptor;
    private final ObjectMapper objectMapper;

    private final String id;

    public static Builder builder() {
        return new Builder();
    }

    DockerServiceImpl(Builder b) {
        //========================= from builder =================================
        this.node = b.node;
        this.cluster = b.cluster;
        Assert.isTrue((this.node == null || this.cluster == null) && this.node != this.cluster,
                "Invalid config of service: ngroup=" + this.cluster + " node=" + node + " service must has only one non null value.");
        this.config = b.config.validate();
        this.restTemplate = b.restTemplate;
        Assert.notNull(this.restTemplate, "restTemplate is null");
        this.nodeInfoProvider = b.nodeInfoProvider;
        Assert.notNull(this.nodeInfoProvider, "nodeInfoProvider is null");
        this.eventConsumer = b.eventConsumer;
        Assert.notNull(this.eventConsumer, "eventConsumer is null");
        this.infoInterceptor = b.infoInterceptor;
        this.objectMapper = b.objectMapper;
        Assert.notNull(this.objectMapper, "objectMapper is null");

        this.id = DockerService.super.getId();  //cache id

    }
}
