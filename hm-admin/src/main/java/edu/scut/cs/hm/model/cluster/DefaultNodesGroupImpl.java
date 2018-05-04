package edu.scut.cs.hm.model.cluster;

import com.google.common.collect.ImmutableSet;
import edu.scut.cs.hm.admin.service.NodeStorage;
import edu.scut.cs.hm.docker.DockerService;
import edu.scut.cs.hm.docker.VirtualDockerService;
import edu.scut.cs.hm.docker.arg.GetContainersArg;
import edu.scut.cs.hm.docker.arg.NodeUpdateArg;
import edu.scut.cs.hm.docker.model.container.DockerContainer;
import edu.scut.cs.hm.docker.res.ServiceCallResult;
import edu.scut.cs.hm.model.container.ContainersManager;
import edu.scut.cs.hm.model.container.ContainerRegistration;
import edu.scut.cs.hm.model.container.ContainerStorage;
import edu.scut.cs.hm.model.ds.DiscoveryStorage;
import edu.scut.cs.hm.model.node.Node;
import edu.scut.cs.hm.model.node.NodeInfo;
import lombok.Builder;
import lombok.Singular;
import lombok.ToString;
import org.springframework.security.access.AccessDeniedException;

import java.util.*;

/**
 * Node group managed 'manually', not like cluster united by docker in swarm mode or swarm.
 * It allow to view multiple nodes as single entity.
 */
@ToString(callSuper = true)
public class DefaultNodesGroupImpl extends AbstractNodesGroup<DefaultNodesGroupConfig> {

    /**
     * @param id name of node, when we add a node use the name
     * @return
     */
    @Override
    public boolean hasNode(String id) {
        return false;
    }

    /**
     * Return copy of all current nodes collection
     *
     * @return copy of current nodes
     */
    @Override
    public List<NodeInfo> getNodes() {
        return null;
    }

    /**
     * Update node but not only attributes of physical node, some 'node in docker swarm cluster' attr
     *
     * @param arg
     * @return
     */
    @Override
    public ServiceCallResult updateNode(NodeUpdateArg arg) {
        return null;
    }

    /**
     * Collections with names of other intersected NodesGroups. Note that it
     * not mean 'enclosed' relationship. <p/>
     * The 'Real Cluster' means 'swarm cluster' or 'docker in swarm mode cluster'
     * Any RealCluster always return empty collection.
     * For example 'all' - return all real clusters
     *
     * @return
     */
    @Override
    public Collection<String> getGroups() {
        return null;
    }

    /**
     * When we use swarm or 'docker in swarm mode' we return manager node's docker service
     *
     * @return
     */
    @Override
    public DockerService getDocker() {
        return null;
    }

    /**
     * Tool for managing cluster containers, it replace for direct access to docker service
     *
     * @return non null value
     */
    @Override
    public ContainersManager getContainers() {
        return null;
    }

    public interface ContainersProvider {
        List<DockerContainer> getContainers(DefaultNodesGroupImpl ng, GetContainersArg arg);
    }

    private static class DefaultContainersProvider implements ContainersProvider {

        @Override
        public List<DockerContainer> getContainers(DefaultNodesGroupImpl ng, GetContainersArg arg) {
            List<DockerContainer> list = new ArrayList<>();
            NodeStorage nodeStorage = ng.getNodeStorage();
            for (Node node: ng.getNodes()) {
                // get docker service from node
                DockerService service = nodeStorage.getDockerService(node.getName());
                if (VirtualDockerService.isOffline(service)) {
                    // due to different causes service can be null
                    continue;
                }
                try {
                    // this will check acl
                    List<DockerContainer> nodeContainer = service.getContainers(arg);
                    list.addAll(nodeContainer);
                } catch (AccessDeniedException e) {
                    // ignore
                }
            }
            return list;
        }
    }

    public static class AllContainersProvider implements ContainersProvider {
        @Override
        public List<DockerContainer> getContainers(DefaultNodesGroupImpl ng, GetContainersArg arg) {
            List<ContainerRegistration> containers = ng.containerStorage.getContainers();
            ArrayList<DockerContainer> list = new ArrayList<>(containers.size());
            containers.forEach(cr -> list.add(cr.getContainer()));
            return list;
        }
    }

    private final ContainersProvider DEFAULT_CONTAINERS_PROVIDER = new DefaultContainersProvider();
    private final VirtualDockerService service;
    private ContainersManager containers;
    private ContainerStorage containerStorage;
    private ContainersProvider containersProvider;

    @Builder
    public DefaultNodesGroupImpl(DiscoveryStorage discoveryStorage,
                                 DefaultNodesGroupConfig config,
                                 @Singular Set<Feature> features) {
        super(config, discoveryStorage, ImmutableSet.<Feature>builder()
                .addAll(features == null ? Collections.emptySet() : features)
                .build());
        this.containersProvider = DEFAULT_CONTAINERS_PROVIDER;
        this.service = new VirtualDockerService(this);
    }

    public ContainerStorage getContainerStorage() {
        return containerStorage;
    }

    public List<DockerContainer> getContainersImpl(GetContainersArg arg) {
        return this.containersProvider.getContainers(this, arg);
    }
}
