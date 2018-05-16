package edu.scut.cs.hm.admin.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import edu.scut.cs.hm.admin.component.FilterFactory;
import edu.scut.cs.hm.admin.security.AccessContext;
import edu.scut.cs.hm.admin.security.AccessContextFactory;
import edu.scut.cs.hm.admin.security.SecuredType;
import edu.scut.cs.hm.admin.security.TempAuth;
import edu.scut.cs.hm.admin.security.acl.AclModifier;
import edu.scut.cs.hm.common.kv.KeyValueStorage;
import edu.scut.cs.hm.common.kv.mapping.KvMap;
import edu.scut.cs.hm.common.kv.mapping.KvMapAdapter;
import edu.scut.cs.hm.common.kv.mapping.KvMapLocalEvent;
import edu.scut.cs.hm.common.kv.mapping.KvMapperFactory;
import edu.scut.cs.hm.common.mb.MessageBus;
import edu.scut.cs.hm.common.security.Authorities;
import edu.scut.cs.hm.common.security.acl.TenantGrantedAuthoritySid;
import edu.scut.cs.hm.common.security.acl.dto.AceSource;
import edu.scut.cs.hm.common.security.acl.dto.AclSource;
import edu.scut.cs.hm.common.security.acl.dto.Action;
import edu.scut.cs.hm.common.utils.Closeables;
import edu.scut.cs.hm.common.utils.ExecutorUtils;
import edu.scut.cs.hm.model.ExtendedAssert;
import edu.scut.cs.hm.common.utils.Throwables;
import edu.scut.cs.hm.docker.DockerService;
import edu.scut.cs.hm.model.StandardAction;
import edu.scut.cs.hm.model.cluster.ClusterFactory;
import edu.scut.cs.hm.model.cluster.DefaultCluster;
import edu.scut.cs.hm.model.ngroup.AbstractNodesGroupConfig;
import edu.scut.cs.hm.model.cluster.ClusterConfigFactory;
import edu.scut.cs.hm.model.ngroup.DefaultNodesGroupConfig;
import edu.scut.cs.hm.model.ngroup.NodesGroup;
import edu.scut.cs.hm.model.ngroup.NodesGroupEvent;
import edu.scut.cs.hm.model.cluster.DiscoveryStorage;
import edu.scut.cs.hm.model.filter.OrphansNodeFilterFactory;
import edu.scut.cs.hm.model.node.NodeEvent;
import edu.scut.cs.hm.model.node.NodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.validation.Validator;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/**
 * Storage service for clusters (nodes group - actual docker cluster or just logic combination of physical nodes)
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
     * @param factory   factory or null. The factory to create {@link edu.scut.cs.hm.model.ngroup.NodesGroupConfig}
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
    public NodesGroup getOrCreateCluster(AbstractNodesGroupConfig<?> config) {
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
        return  new ClusterFactory(this, beanFactory, validator);
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
     * @return the set of clusters' name
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
     * Get NodeService
     * @return
     */
    public NodeStorage getNodeStorage() {
        return nodeStorage;
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

    /**
     * Get NodesGroup KvMap
     * @return
     */
    public KvMap<NodesGroup> getKvMap() {
        return clusters;
    }

    /**
     * Get executor
     * @return
     */
    public ExecutorService getExecutor() {
        return executor;
    }


    private final DockerServices dockerServices;
    private final NodeStorage nodeStorage;
    private final KvMap<NodesGroup> clusters;
    private final AccessContextFactory aclContextFactory;
    private final MessageBus<NodesGroupEvent> messageBus;
    private final AutowireCapableBeanFactory beanFactory;
    private final ExecutorService executor;
    private final Validator validator;

    @Autowired
    public DiscoveryStorageImpl(KvMapperFactory kvmf,
                                FilterFactory filterFactory,
                                DockerServices dockerServices,
                                NodeStorage nodeStorage,
                                AccessContextFactory aclContextFactory,
                                AutowireCapableBeanFactory beanFactory,
                                Validator validator,
                                @Qualifier(NodesGroupEvent.BUS) MessageBus<NodesGroupEvent> messageBus) {
        this.beanFactory = beanFactory;
        this.dockerServices = dockerServices;
        this.nodeStorage = nodeStorage;
        this.messageBus = messageBus;
        this.aclContextFactory = aclContextFactory;
        this.validator = validator;

        KeyValueStorage storage = kvmf.getStorage();
        String prefix = storage.getPrefix() + "/clusters/";
        this.clusters = KvMap.builder(NodesGroup.class, AbstractNodesGroupConfig.class)
                .path(prefix)
                .mapper(kvmf)
                .passDirty(true)
                .adapter(new KvMapAdapterImpl())
                .listener(e -> {                                        // this event only from remote k-v storage
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
                .localListener(e -> {                                  // this event from remote k-v storage and local k-v map
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

    /**
     * load clusters, this will be used when hm-admin application startup
     * @see edu.scut.cs.hm.admin.bootstrap.ApplicationBootstrap
     */
    public void load() {
        try (TempAuth au = TempAuth.asSystem()) {
            log.info("Begin load clusters from k-v storage");

            //load keys, and then init values
            clusters.load();
            // load values
            Collection<NodesGroup> values = clusters.values();
            StringBuilder sb = new StringBuilder();
            values.forEach(ng -> {
                sb.append("\n");
                sb.append(ng.getName()).append(":\n\ttitle:");
                sb.append(ng.getTitle()).append("\n\tconfig:");
                sb.append(ng.getConfig());
            });
            log.warn("Loaded clusters from k-v storage: {}", sb);
        } catch (Exception e) {
            log.error("Can not load clusters from k-v storage", e);
        }
    }

    @PostConstruct
    public void init() {
        try (TempAuth au = TempAuth.asSystem()) {
            // modifier acl source to add default ace 'granting ROLE_USER read'
            // for two default cluster below
            AclModifier aclModifier = this::addDefaultAce;

            // virtual cluster for any nodes
            NodesGroup allGroup = getOrCreateCluster(new DefaultNodesGroupConfig(GROUP_ID_ALL, FilterFactory.ANY));
            allGroup.updateAcl(aclModifier);
            ((DefaultCluster)allGroup).setContainersProvider(new DefaultCluster.AllContainersProvider());

            // virtual cluster for nodes without cluster
            NodesGroup orphansGroup = getOrCreateCluster(new DefaultNodesGroupConfig(GROUP_ID_ORPHANS, OrphansNodeFilterFactory.FILTER));
            orphansGroup.updateAcl(aclModifier);
        }

        // subscribe node event listener
        getNodeStorage().getNodeEventSubscriptions().subscribe(this::onNodeEvent);
    }

    private void onNodeEvent(NodeEvent nodeEvent) {
        if (nodeEvent.getAction().isPre()) {
            return;
        }

        NodeInfo node = nodeEvent.getCurrent();
        if (node == null) {
            return;
        }

        String clusterName = node.getCluster();
        if (clusterName == null) {
            return;
        }

        NodesGroup cluster = getCluster(clusterName);
        if (cluster == null) {
            log.warn("Node {} without cluster", node);
            return;
        }
        executor.execute(() -> {
            // setup cluster (it need only cases when cluster have no one node, for both types of clusters)
            try (TempAuth au = TempAuth.asSystem()) {
                cluster.init();
            }
        });
    }

    private boolean addDefaultAce(AclSource.Builder asb) {
        // here we add default rights to read groups by all users;
        TenantGrantedAuthoritySid tgs = TenantGrantedAuthoritySid.from(Authorities.USER);
        Action read = Action.READ;
        for (AceSource ace: asb.getEntries().values()) {
            if (ace.isGranting() && tgs.equals(ace.getSid()) && ace.getPermission().getMask() == read.getMask()) {
                return false;
            }
        }
        asb.addEntry(AceSource.builder()
                .permission(read)
                .granting(true)
                .sid(tgs)
                .build());
        return true;
    }

    // 发射GroupEvent
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
