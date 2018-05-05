package edu.scut.cs.hm.admin.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import edu.scut.cs.hm.admin.component.FilterFactory;
import edu.scut.cs.hm.admin.security.AccessContext;
import edu.scut.cs.hm.admin.security.AccessContextFactory;
import edu.scut.cs.hm.admin.security.SecuredType;
import edu.scut.cs.hm.admin.security.TempAuth;
import edu.scut.cs.hm.common.kv.KeyValueStorage;
import edu.scut.cs.hm.common.kv.mapping.KvMap;
import edu.scut.cs.hm.common.kv.mapping.KvMapAdapter;
import edu.scut.cs.hm.common.kv.mapping.KvMapLocalEvent;
import edu.scut.cs.hm.common.kv.mapping.KvMapperFactory;
import edu.scut.cs.hm.common.mb.MessageBus;
import edu.scut.cs.hm.common.security.acl.dto.Action;
import edu.scut.cs.hm.common.utils.Closeables;
import edu.scut.cs.hm.common.utils.ExecutorUtils;
import edu.scut.cs.hm.common.utils.ExtendedAssert;
import edu.scut.cs.hm.common.utils.Throwables;
import edu.scut.cs.hm.docker.DockerService;
import edu.scut.cs.hm.model.StandardAction;
import edu.scut.cs.hm.model.cluster.ClusterFactory;
import edu.scut.cs.hm.model.ngroup.AbstractNodesGroupConfig;
import edu.scut.cs.hm.model.cluster.ClusterConfigFactory;
import edu.scut.cs.hm.model.ngroup.NodesGroup;
import edu.scut.cs.hm.model.ngroup.NodesGroupEvent;
import edu.scut.cs.hm.model.cluster.DiscoveryStorage;
import edu.scut.cs.hm.model.filter.OrphansNodeFilterFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/**
 * Cluster discovery storage implementation
 */
@Slf4j
@Primary
@Component
public class DiscoveryStorageImpl implements DiscoveryStorage {


    /**
     * Return exists ngroup of concrete node.
     *
     * @param node
     * @return ngroup or null
     */
    @Override
    public NodesGroup getClusterForNode(String node) {
        NodesGroup cluster = findNodeCluster(node);
        checkThatCanRead(cluster);
        return cluster;
    }

    private NodesGroup findNodeCluster(String node) {
        Assert.notNull(node, "node is null");
        //we need resolve real cluster or orphans otherwise
        String nodeCluster = nodeStorage.getNodeCluster(node);
        if(nodeCluster == null) {
            return null;
        }
        return clusters.get(nodeCluster);
    }

    /**
     * Return exists ngroup or null
     *
     * @param clusterId
     * @return exists ngroup or null
     */
    @Override
    public NodesGroup getCluster(String clusterId) {
        NodesGroup ng = clusters.get(clusterId);
        checkThatCanRead(ng);
        return ng;
    }

    /**
     *  get or create cluster. Consumer('factory') will be invoked before cluster process start and allow modification of swarm parameters
     *
     * @param clusterId name of ngroup
     * @param factory   factory or null
     * @return NodesGroup, never null
     */
    @Override
    public NodesGroup getOrCreateCluster(String clusterId, ClusterConfigFactory factory) {
        ExtendedAssert.matchAz09Hyp(clusterId, "clusterId");
        NodesGroup ng = clusters.computeIfAbsent(clusterId, (cid) -> {
            checkThatCanCreate();
            return clusterFactory().configFactory(factory).build(cid);
        });
        // we place it after creation, because check it for not created cluster is not good
        checkThatCanRead(ng);
        return ng;
    }

    /**
     * Register new group, or return already registered. Like {@link #getOrCreateCluster(String, ClusterConfigFactory)} but allow to
     * create node group and real clusters too.
     *
     * @param config
     * @return registered node group.
     */
    @Override
    public NodesGroup getOrCreateGroup(AbstractNodesGroupConfig<?> config) {
        final String clusterId = config.getName();
        ExtendedAssert.matchAz09Hyp(clusterId, "clusterId");
        NodesGroup ng = clusters.computeIfAbsent(clusterId, (cid) -> {
            checkThatCanCreate();
            return makeClusterFromConfig(config, cid);
        });
        // we place it after creation, because check it for not created cluster is not good
        checkThatCanRead(ng);
        return ng;
    }

    private NodesGroup makeClusterFromConfig(AbstractNodesGroupConfig<?> config, String cid) {
        return clusterFactory().config(config).build(cid);
    }

    private ClusterFactory clusterFactory() {
        return  new ClusterFactory(this, beanFactory);
    }

    /**
     * Delete ngroup by ngroup name (real ngroup)
     *
     * @param clusterId ngroup name {@link NodesGroup#getName()}
     */
    @Override
    public void deleteCluster(String clusterId) {
        NodesGroup cluster = clusters.get(clusterId);
        ExtendedAssert.notFound(cluster, "Cluster: " + clusterId + " is not found.");
        Assert.isTrue(!isPredefined(cluster), "Can not delete predefined cluster: " + clusterId);
        deleteGroup(clusterId);
    }

    /**
     * Delete ngroup by nodes group name (just in logic nodes group not real ngroup)
     *
     * @param clusterId nodes group name {@link NodesGroup#getName()}
     */
    @Override
    public void deleteNodeGroup(String clusterId) {
        NodesGroup cluster = clusters.get(clusterId);
        Assert.notNull(cluster, "GroupId: " + clusterId + " is not found.");
        Assert.isTrue(!isPredefined(cluster), "Can't delete predefined cluster");
        deleteGroup(clusterId);
    }

    private boolean isPredefined(NodesGroup nodesGroup) {
        String name = nodesGroup.getName();
        return SYSTEM_GROUPS.contains(name);
    }

    private void deleteGroup(String clusterId) {
        aclContextFactory.getContext().assertGranted(SecuredType.CLUSTER.id(clusterId), Action.DELETE);
        NodesGroup ng = clusters.remove(clusterId);
        handleRemoved(ng);
        log.error("Delete '{}' cluster.", clusterId);
    }

    private void handleRemoved(NodesGroup ng) {
        if(ng == null) {
            return;
        }
        ng.clean();
        Closeables.closeIfCloseable(ng);
    }


    /**
     * Get all ngroup (include nodes group and real ngroup)
     *
     * @return
     */
    @Override
    public List<NodesGroup> getClusters() {
        ImmutableList.Builder<NodesGroup> ilb = ImmutableList.builder();
        AccessContext ac = aclContextFactory.getContext();
        this.clusters.forEach((k, v) -> {
            if(ac.isGranted(v.getOid(), Action.READ)) {
                ilb.add(v);
            }
        });
        return ilb.build();
    }

    /**
     * Docker service
     *
     * @param clusterId
     * @return
     */
    @Override
    public DockerService getService(String clusterId) {
        Assert.hasText(clusterId, "Name of cluster is null or empty");
        NodesGroup eurekaCluster = clusters.get(clusterId);
        ExtendedAssert.notFound(eurekaCluster, "Registry does not contains service with clusterId: " + clusterId);
        return eurekaCluster.getDocker();
    }

    /**
     * In docker swarm mode service means a combination of containers(maybe in different nodes)
     * some like {@link #getClusters()}
     * @return
     */
    @Override
    public Set<String> getServices() {
        ImmutableSet.Builder<String> isb = ImmutableSet.builder();
        AccessContext ac = aclContextFactory.getContext();
        this.clusters.forEach((k, v) -> {
            if(ac.isGranted(v.getOid(), Action.READ)) {
                isb.add(k);
            }
        });
        return isb.build();
    }

    /**
     * Get executor
     * @return
     */
    public ExecutorService getExecutor() {
        return executor;
    }

    /**
     * Get NodeService
     * @return
     */
    public NodeStorage getNodeStorage() {
        return nodeStorage;
    }

    public KvMap<NodesGroup> getKvMap() {
        return clusters;
    }

    /**
     * Return DockerServices
     * @return
     */
    public DockerServices getDockerServices() {
        return this.dockerServices;
    }

    /**
     * Need only for {@link edu.scut.cs.hm.admin.component.ClusterAclProvider} - it prevent recursion on security checks.
     * @param clusterId
     * @return
     */
    public NodesGroup getClusterBypass(String clusterId) {
        return clusters.get(clusterId);
    }

    /**
     * It need only for {@link edu.scut.cs.hm.admin.component.ClusterAclProvider}
     * @param consumer
     */
    public void getClustersBypass(Consumer<NodesGroup> consumer) {
        this.clusters.forEach((k, v) -> consumer.accept(v));
    }


    private final DockerServices dockerServices;
    private final NodeStorage nodeStorage;
    private final KvMap<NodesGroup> clusters;
    private final AccessContextFactory aclContextFactory;
    private final MessageBus<NodesGroupEvent> messageBus;
    private final AutowireCapableBeanFactory beanFactory;
    private final ExecutorService executor;

    @Autowired
    public DiscoveryStorageImpl(KvMapperFactory kvmf,
                                FilterFactory filterFactory,
                                DockerServices dockerServices,
                                NodeStorage nodeStorage,
                                AccessContextFactory aclContextFactory,
                                AutowireCapableBeanFactory beanFactory,
                                @Qualifier(NodesGroupEvent.BUS) MessageBus<NodesGroupEvent> messageBus) {
        this.beanFactory = beanFactory;
        this.dockerServices = dockerServices;
        this.nodeStorage = nodeStorage;
        this.messageBus = messageBus;
        this.aclContextFactory = aclContextFactory;

        KeyValueStorage storage = kvmf.getStorage();
        String prefix = storage.getPrefix() + "/clusters/";
        this.clusters = KvMap.builder(NodesGroup.class, AbstractNodesGroupConfig.class)
                .path(prefix)
                .mapper(kvmf)
                .passDirty(true)
                .adapter(new KvMapAdapterImpl())
                .listener(e -> {
                    String key = e.getKey();
                    switch (e.getAction()) {
                        case DELETE:
                            fireGroupEvent(key, StandardAction.DELETE);
                            break;
                        case CREATE:
                            fireGroupEvent(key, StandardAction.CREATE);
                            break;
                        case UPDATE:
                            fireGroupEvent(key, StandardAction.UPDATE);
                    }
                })
                .localListener(e -> {
                    KvMapLocalEvent.Action action = e.getAction();
                    switch (action) {
                        case CREATE:
                            checkThatCanCreate();
                            break;
                        case DELETE:
                            // delete usually caused by kv event, and not has context
                            try(TempAuth ta = TempAuth.asSystem()) {
                                handleRemoved(e.getOldValue());
                            }
                            break;
                    }
                })
                .build();
        filterFactory.registerFilter(new OrphansNodeFilterFactory(this));
        this.executor = ExecutorUtils.executorBuilder()
                .coreSize(1).maxSize(10 /*possible max count of clusters*/)
                .exceptionHandler(Throwables.uncaughtHandler(log))
                .daemon(true)
                .name(getClass().getSimpleName())
                .build();
    }

    private void fireGroupEvent(String clusterId, StandardAction action) {
        NodesGroupEvent.Builder logEvent = new NodesGroupEvent.Builder();
        logEvent.setAction(action.value());
        logEvent.setCluster(clusterId);
        logEvent.setSeverity(StandardAction.toSeverity(action.value()));
        fireGroupEvent(logEvent);
    }

    private void fireGroupEvent(NodesGroupEvent.Builder eventBuilder) {
        messageBus.accept(eventBuilder.build());
    }

    private void checkThatCanCreate() {
        aclContextFactory.getContext().assertGranted(SecuredType.CLUSTER.typeId(), Action.CREATE);
    }

    private void checkThatCanRead(NodesGroup ng) {
        if(ng == null) {
            return;
        }
        aclContextFactory.getContext().assertGranted(SecuredType.CLUSTER.id(ng.getName()), Action.READ);
    }

    private class KvMapAdapterImpl implements KvMapAdapter<NodesGroup> {
        @Override
        public Object get(String key, NodesGroup src) {
            return src.getConfig();
        }

        @Override
        public NodesGroup set(String key, NodesGroup src, Object value) {
            AbstractNodesGroupConfig<?> config = (AbstractNodesGroupConfig<?>) value;
            if(src != null) {
                src.setConfig(config);
            } else {
                src = makeClusterFromConfig(config, key);
            }
            return src;
        }

        @Override
        public Class<?> getType(NodesGroup src) {
            if(src == null) {
                return null;
            }
            AbstractNodesGroupConfig<?> config = src.getConfig();
            if(config != null) {
                return config.getClass();
            }
            //src.getClass().getTypeParameters();
            return null;
        }
    }

}
