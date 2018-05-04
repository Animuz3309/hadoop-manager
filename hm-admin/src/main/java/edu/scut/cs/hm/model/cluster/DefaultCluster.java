package edu.scut.cs.hm.model.cluster;

import com.google.common.collect.ImmutableSet;
import edu.scut.cs.hm.admin.component.ContainerCreator;
import edu.scut.cs.hm.admin.component.FilterFactory;
import edu.scut.cs.hm.admin.service.DiscoveryStorageImpl;
import edu.scut.cs.hm.admin.service.NodeStorage;
import edu.scut.cs.hm.docker.DockerService;
import edu.scut.cs.hm.docker.VirtualDockerService;
import edu.scut.cs.hm.docker.arg.GetContainersArg;
import edu.scut.cs.hm.docker.arg.NodeUpdateArg;
import edu.scut.cs.hm.docker.model.container.DockerContainer;
import edu.scut.cs.hm.docker.res.ServiceCallResult;
import edu.scut.cs.hm.model.container.ContainerRegistration;
import edu.scut.cs.hm.model.container.ContainerStorage;
import edu.scut.cs.hm.model.container.ContainersManager;
import edu.scut.cs.hm.model.container.SwarmClusterContainers;
import edu.scut.cs.hm.model.filter.Filter;
import edu.scut.cs.hm.model.ngroup.AbstractNodesGroup;
import edu.scut.cs.hm.model.ngroup.DefaultNodesGroupConfig;
import edu.scut.cs.hm.model.node.Node;
import edu.scut.cs.hm.model.node.NodeInfo;
import lombok.Builder;
import lombok.Singular;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;

import java.util.*;

/**
 * Node group managed 'manually', not like ngroup united by docker in swarm mode or swarm.
 * It allow to view multiple nodes as single entity.
 *
 * with DockerService {@link VirtualDockerService} and ContainersManager {@link SwarmClusterContainers}
 */
@ToString(callSuper = true)
public class DefaultCluster extends AbstractNodesGroup<DefaultNodesGroupConfig> {

    /**
     * @param id name of node, when we add a node use the name
     * @return
     */
    @Override
    public boolean hasNode(String id) {
        return getNodeStorage().hasNode(predicate, id);
    }

    /**
     * Return copy of all current nodes collection
     *
     * @return copy of current nodes
     */
    @Override
    public List<NodeInfo> getNodes() {
        return getNodesInternal();
    }

    /**
     * Not supported
     *
     * @return not support result
     */
    @Override
    public ServiceCallResult updateNode(NodeUpdateArg arg) {
        return ServiceCallResult.unsupported();
    }

    /**
     * Get the collection of nodes's ngroup name which satisfied filter {@link #predicate}
     *
     * @return
     */
    @Override
    public Collection<String> getGroups() {
        Set<String> clusters = new HashSet<>();
        getNodesInternal().forEach(n -> clusters.add(n.getCluster()));
        return clusters;
    }

    /**
     * Return {@link VirtualDockerService}
     *
     * @return
     */
    @Override
    public DockerService getDocker() {
        return this.service;
    }

    /**
     * Tool for managing ngroup containers, it replace for direct access to docker service
     *
     * @return non null value
     */
    @Override
    public ContainersManager getContainers() {
        return this.containers;
    }

    public interface ContainersProvider {
        List<DockerContainer> getContainers(DefaultCluster ng, GetContainersArg arg);
    }

    private static class DefaultContainersProvider implements ContainersProvider {

        @Override
        public List<DockerContainer> getContainers(DefaultCluster ng, GetContainersArg arg) {
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
        public List<DockerContainer> getContainers(DefaultCluster ng, GetContainersArg arg) {
            List<ContainerRegistration> containers = ng.containerStorage.getContainers();
            ArrayList<DockerContainer> list = new ArrayList<>(containers.size());
            containers.forEach(cr -> list.add(cr.getContainer()));
            return list;
        }
    }

    private final ContainersProvider DEFAULT_CONTAINERS_PROVIDER = new DefaultContainersProvider();
    private final VirtualDockerService service;

    private Filter predicate;
    private FilterFactory filterFactory;        // autowired
    private ContainersManager containers;
    private ContainerStorage containerStorage;  // autowired
    private ContainerCreator containerCreator;  // autowired
    private ContainersProvider containersProvider;


    @Builder
    DefaultCluster(DiscoveryStorageImpl storage,
                   Filter predicate,
                   DefaultNodesGroupConfig config,
                   @Singular Set<Feature> features) {
        super(config, storage, ImmutableSet.<Feature>builder()
                .addAll(features == null ? Collections.emptySet() : features)
                .build());
        this.containersProvider = DEFAULT_CONTAINERS_PROVIDER;
        this.service = new VirtualDockerService(this);

        this.predicate = predicate;
    }

    @Override
    protected void initImpl() {
        if (predicate != null) {
            config.setNodeFilter(predicate.getExpression());
        } else {
            this.predicate = filterFactory.createFilter(config.getNodeFilter());
        }
        this.containers = new SwarmClusterContainers(this::getDocker, this.containerCreator);
    }

    /**
     * @see ClusterFactory#build(String)
     * @param containerStorage
     */
    // beanFactory动态注入，而不是一开始就由框架产生
    @Autowired
    void setContainerStorage(ContainerStorage containerStorage) {
        this.containerStorage = containerStorage;
    }

    /**
     * @see ClusterFactory#build(String)
     * @param containerCreator
     */
    // beanFactory动态注入，而不是一开始就由框架产生
    @Autowired
    void setContainerCreator(ContainerCreator containerCreator) {
        this.containerCreator = containerCreator;
    }

    /**
     * @see ClusterFactory#build(String)
     * @param filterFactory
     */
    // beanFactory动态注入，而不是一开始就由框架产生
    @Autowired
    void setFilterFactory(FilterFactory filterFactory) {
        this.filterFactory = filterFactory;
    }

    public void setContainersProvider(ContainersProvider containersProvider) {
        this.containersProvider = containersProvider;
    }

    public ContainerStorage getContainerStorage() {
        return containerStorage;
    }

    public List<DockerContainer> getContainersImpl(GetContainersArg arg) {
        return this.containersProvider.getContainers(this, arg);
    }

    private List<NodeInfo> getNodesInternal() {
        return getNodeStorage().getNodes(predicate);
    }
}
