package edu.scut.cs.hm.admin.service;

import com.google.common.collect.ImmutableSet;
import edu.scut.cs.hm.admin.component.DockerServiceFactory;
import edu.scut.cs.hm.admin.security.AccessContext;
import edu.scut.cs.hm.admin.security.AccessContextFactory;
import edu.scut.cs.hm.admin.security.SecuredType;
import edu.scut.cs.hm.admin.security.TempAuth;
import edu.scut.cs.hm.common.kv.KeyValueStorage;
import edu.scut.cs.hm.common.kv.KvStorageEvent;
import edu.scut.cs.hm.common.kv.mapping.KvMap;
import edu.scut.cs.hm.common.kv.mapping.KvMapEvent;
import edu.scut.cs.hm.common.kv.mapping.KvMapLocalEvent;
import edu.scut.cs.hm.common.kv.mapping.KvMapperFactory;
import edu.scut.cs.hm.common.mb.MessageBus;
import edu.scut.cs.hm.common.mb.Subscriptions;
import edu.scut.cs.hm.common.security.acl.dto.Action;
import edu.scut.cs.hm.common.utils.ExecutorUtils;
import edu.scut.cs.hm.common.utils.ExtendedAssert;
import edu.scut.cs.hm.docker.DockerConfig;
import edu.scut.cs.hm.docker.DockerService;
import edu.scut.cs.hm.docker.model.events.DockerEventConfig;
import edu.scut.cs.hm.docker.model.events.DockerLogEvent;
import edu.scut.cs.hm.model.HttpException;
import edu.scut.cs.hm.model.node.*;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Node service for save/load physical node
 */
@Slf4j
@Data
@Service
public class NodeStorage implements NodeInfoProvider, NodeRegistry {
    @Getter private final NodeStorageConfig nodeStorageConfig;
    @Getter private final DockerEventConfig dockerEventConfig;
    private final MessageBus<NodeEvent> nodeEventBus;
    private final MessageBus<DockerLogEvent> dockerLogBus;
    private final KvMap<NodeRegistrationImpl> nodes;
    private final ExecutorService executor;

    private DockerServiceFactory dockerServiceFactory;

    @Autowired
    public NodeStorage(NodeStorageConfig nodeStorageConfig,
                       DockerEventConfig dockerEventConfig,
                       KvMapperFactory kvmf,
                       @Qualifier(NodeEvent.BUS) MessageBus<NodeEvent> nodeEventBus,
                       @Qualifier(DockerLogEvent.BUS) MessageBus<DockerLogEvent> dockerLogBus) {
        this.nodeStorageConfig = nodeStorageConfig;                // get NodeStorageConfig
        this.dockerEventConfig = dockerEventConfig;                // get DockerEventConfig
        this.nodeEventBus = nodeEventBus;
        this.dockerLogBus = dockerLogBus;

        KeyValueStorage storage = kvmf.getStorage();
        String nodesPrefix = storage.getPrefix() + "/nodes/";
        this.nodes = KvMap.builder(NodeRegistrationImpl.class, NodeInfoImpl.Builder.class)
                .path(nodesPrefix)
                .passDirty(true)
                .adapter(new NodesKvMapAdapterImpl(this))
                .localListener((e) -> {
                    if (e.getAction() == KvMapLocalEvent.Action.CREATE) {
                        AccessContextFactory.getLocalContext().assertGranted(SecuredType.NODE.id(e.getKey()), Action.CREATE);
                    }
                })
                .listener(this::onKVEvent)
                .mapper(kvmf)
                .build();

        log.info("{} initialized with nodeStorageConfig: {}", getClass().getSimpleName(), this.nodeStorageConfig);

        this.executor = ExecutorUtils.executorBuilder()
                .name(getClass().getSimpleName())
                .maxSize(this.nodeStorageConfig.getMaxNodes())
                .rejectedHandler((runnable, executor) -> {
                    String hint = "";
                    try {
                        int nodes = this.nodes.list().size();
                        int maxNodes = this.nodeStorageConfig.getMaxNodes();
                        if(nodes > maxNodes) {
                            hint = "\nNote that 'nodeStorageConfig.maxNodes'=" + maxNodes + " but volume has 'node'=" + nodes;
                        }
                    } catch (Exception e) {
                        //supress
                    }
                    throw new RejectedExecutionException("Task " + runnable + " rejected from " + executor + hint);
                })
                .build();

    }

    @Autowired
    @Lazy
    void setDockerFactory(DockerServiceFactory dockerFactory) {
        this.dockerServiceFactory = dockerFactory;
    }

    private void onKVEvent(KvMapEvent<NodeRegistrationImpl> e) {
        String key = e.getKey();
        KvStorageEvent.Crud action = e.getAction();
        try (TempAuth ta = TempAuth.asSystem()) {
            switch (action) {
                case DELETE: {
                    NodeRegistrationImpl nr = e.getValue();
                    NodeInfoImpl ni = nr == null? NodeInfoImpl.builder().name(key).build() : nr.getNodeInfo();
                    fireNodeModification(nr, NodeEvent.Action.DELETE, ni, null);
                    break;
                }
                default: {
                    NodeRegistrationImpl nr = this.nodes.getIfPresent(key);
                    // update event will send from node registration
                    if (nr != null && action == KvStorageEvent.Crud.CREATE) {
                        fireNodeModification(nr, NodeEvent.Action.CREATE, null, nr.getNodeInfo());
                    }
                }
            }
        }
    }

    @PostConstruct
    public void init() {
        nodes.load();   // load all node from k-v volume
    }

    /**
     * @return true when {@link NodeEvent#cancel()} was called in one of {@link #nodeEventBus} event consumers
     */
    public boolean fireNodePreModification(NodeInfoImpl old, NodeInfoImpl curr) {
        AtomicBoolean cancel = new AtomicBoolean(false);
        NodeEvent ne = NodeEvent.builder()
                .canceller(() -> cancel.set(true))
                .action(NodeEvent.Action.PRE_UPDATE)
                .old(old)
                .current(curr)
                .build();
        fireNodeEventSync(ne);
        return cancel.get();
    }

    /**
     * {@link NodeEvent} is accept in {@link #nodeEventBus}
     */
    public void fireNodeModification(NodeRegistrationImpl nr, NodeEvent.Action action, NodeInfoImpl old, NodeInfoImpl current) {
        if (old == null && current == null) {
            log.error("Something wrong:  old and current values of node '{}' is null, at action '{}'", nr.getName(), action.name());
            return;
        }

        NodeEvent ne = NodeEvent.builder()
                .action(action)
                .current(current)
                .old(old)
                .build();
        this.executor.execute(() -> {
            try (TempAuth auth = TempAuth.asSystem()) {
                fireNodeEventSync(ne);
            }
        });
    }

    private void fireNodeEventSync(NodeEvent ne) {
        this.nodeEventBus.accept(ne);
    }

    /**
     * Create new {@link NodeRegistrationImpl}
     * @param nodeInfo
     * @return
     */
    public NodeRegistrationImpl newRegistration(NodeInfo nodeInfo) {
        NodeRegistrationImpl nr = new NodeRegistrationImpl(this, nodeInfo);
        nr.setTtl(this.nodeStorageConfig.getUpdateSeconds() * 2);
        nr.init();
        return nr;
    }

    /**
     * Judge there is a node that of 'nodeId' and 'predicate#test()' is true
     * @param predicate
     * @param nodeId
     * @return
     */
    public boolean hasNode(Predicate<Object> predicate, String nodeId) {
        NodeRegistration instance = getNodeRegistrationInternal(nodeId);
        return instance != null && predicate.test(instance);
    }

    /**
     * Get the node of 'nodeId'
     * @param nodeId
     * @return
     */
    public NodeRegistration getNodeRegistration(String nodeId) {
        NodeRegistrationImpl nr = getNodeRegistrationInternal(nodeId);
        checkAccess(nr, Action.READ);
        return nr;
    }

    private void checkAccess(NodeRegistrationImpl nr, Action read) {
        if(nr == null) {
            return;
        }
        AccessContextFactory.getLocalContext().assertGranted(nr.getOid(), read);
    }

    /**
     * Set node's ngroup
     * @param nodeName
     * @param cluster
     * @return
     */
    public NodeInfo setNodeCluster(String nodeName, String cluster) {
        NodeRegistrationImpl nr = getNodeRegistrationInternal(nodeName);
        if(nr == null) {
            throw new HttpException(HttpStatus.NOT_FOUND, "Node '" + nodeName + "' is not found");
        }
        checkAccess(nr, Action.UPDATE);
        NodeInfo ni = nr.getNodeInfo();
        String oldCluster = ni.getCluster();
        if(!Objects.equals(oldCluster, cluster)) {
            //here may be race condition
            nr.setCluster(cluster);
            save(nr);
        }
        return nr.getNodeInfo();
    }

    /**
     * Get node's ngroup
     * @param node
     * @return
     */
    @Override
    public String getNodeCluster(String node) {
        NodeRegistrationImpl nr = getNodeRegistrationInternal(node);
        if (nr == null) {
            return null;
        }
        return nr.getCluster();
    }

    /**
     * Get node's {@link NodeInfo}
     * @param node name of node
     * @return
     */
    @Override
    public NodeInfo getNodeInfo(String node) {
        NodeRegistrationImpl instance = getNodeRegistrationInternal(node);
        if(instance == null) {
            return null;
        }
        checkAccess(instance, Action.READ);
        return instance.getNodeInfo();
    }

    /**
     *
     * @param predicate functor which is return true for InstanceInfo's which will be passed to result.
     * @return
     */
    public List<NodeInfo> getNodes(Predicate<? super NodeRegistration> predicate) {
        Set<String> keys = nodes.list();
        AccessContext ac = AccessContextFactory.getLocalContext();
        List<NodeInfo> nodeList = new ArrayList<>(keys.size());
        for (String key : keys) {
            NodeRegistrationImpl nr = getNodeRegistrationInternal(key);
            // when node invalid we may receive null
            if (nr == null || !predicate.test(nr) || !ac.isGranted(nr.getOid(), Action.READ)) {
                continue;
            }
            nodeList.add(nr.getNodeInfo());
        }
        nodeList.sort(null);
        return nodeList;
    }

    /**
     * Get name of all node
     * @return
     */
    public Collection<String> getNodeNames() {
        return ImmutableSet.copyOf(nodes.list());
    }

    /**
     * Variant of {@link #getNodes(Predicate)} without creation of temp collections & etc.
     * @param consumer
     */
    public void forEach(Consumer<NodeRegistration> consumer) {
        forEachInternal(consumer::accept);
    }

    void forEachInternal(Consumer<NodeRegistrationImpl> consumer) {
        Set<String> keys = nodes.list();
        AccessContext ac = AccessContextFactory.getLocalContext();
        for (String key : keys) {
            NodeRegistrationImpl nr = getNodeRegistrationInternal(key);
            // when node invalid we may receive null
            if (nr == null || !ac.isGranted(nr.getOid(), Action.READ)) {
                continue;
            }
            consumer.accept(nr);
        }
    }

    /**
     * Register node
     * @param nodeName
     * @param address
     * @return
     */
    public DockerService registerNode(String nodeName, String address) {
        NodeRegistrationImpl nr = getByAddress(address);
        if(nr != null) {
            String existsName = nr.getName();
            if(existsName.equals(nodeName)) {
                return nr.getDocker();
            }
            throw new IllegalArgumentException("Can not register '" + nodeName +
                    "', because already has node '" + existsName + "' with same address: " + address);
        }
        nr = getOrCreateNodeRegistration(nodeName);
        DockerService ds = nr.setAddress(address);
        save(nr);
        return ds;
    }

    private NodeRegistrationImpl getByAddress(String address) {
        return nodes.values().stream().filter(nr -> address.equals(nr.getAddress())).findFirst().orElse(null);
    }

    /**
     * Remove node
     * @param name
     */
    public void removeNode(String name) {
        //we check name for prevent names like '../'
        NodeUtils.checkName(name);
        NodeRegistrationImpl nr = getNodeRegistrationInternal(name);
        checkAccess(nr, Action.DELETE);
        AtomicBoolean cancel = new AtomicBoolean(false);
        NodeEvent ne = NodeEvent.builder()
                .action(NodeEvent.Action.PRE_DELETE)
                .old(nr.getNodeInfo())
                .canceller(() -> cancel.set(true))
                .build();
        fireNodeEventSync(ne);
        if(!cancel.get()) {
            nodes.remove(name);
        }
    }

    private NodeRegistrationImpl getNodeRegistrationInternal(String nodeId) {
        if (nodeId == null) {
            return null;
        }
        try {
            return nodes.get(nodeId);
        } catch (Exception e) {
            log.error("Not load node '{}' due validation error: {}", nodeId, e);
        }
        return null;
    }

    /**
     * Register or update node.
     * @param name name of node
     * @param ttl time while for node info is actual
     * @param updater handler which do node update
     */
    public NodeRegistration updateNode(String name, int ttl, Consumer<NodeInfoImpl.Builder> updater) {
        NodeRegistrationImpl nr = getOrCreateNodeRegistration(name);
        nr.setTtl(ttl);// important that it must be before other update methods
        nr.updateNodeInfo(updater);
        save(nr);
        return nr;
    }

    private void save(NodeRegistrationImpl nr) {
        Assert.notNull(nr, "NodeRegistrationImpl is null");
        // we use copy of node info, for data consistency
        nodes.flush(nr.getName());
    }

    private NodeRegistrationImpl getOrCreateNodeRegistration(String name) {
        ExtendedAssert.matchAz09Hyp(name, "node name");
        return nodes.computeIfAbsent(name, (n) -> {
            AccessContextFactory.getLocalContext().assertGranted(SecuredType.NODE.id(name), Action.CREATE);
            return newRegistration(NodeInfoImpl.builder().name(name));
        });
    }

    @Override
    public DockerService getDockerService(String nodeName) {
        NodeRegistrationImpl nr = nodes.get(nodeName);
        if(nr == null) {
            return null;
        }
        return nr.getDocker();
    }

    /**
     * Return node event subscriptions, u can subscribe listener for {@link NodeEvent}
     * {@link #fireNodeEventSync(NodeEvent)} to accept event
     * @return
     */
    public Subscriptions<NodeEvent> getNodeEventSubscriptions() {
        return nodeEventBus.asSubscriptions();
    }

    /**
     * create docker service of node
     * @param nr
     * @return
     */
    public DockerService createNodeDockerService(NodeRegistrationImpl nr) {
        // we intentionally register node without specifying ngroup
        DockerConfig config = DockerConfig.builder().host(nr.getAddress()).build();
        return dockerServiceFactory.createDockerService(config, this, (b) -> b.setNode(nr.getName()));
    }

    /**
     * accept {@link DockerLogEvent}
     * @param e
     */
    public void acceptDockerLogEvent(DockerLogEvent e) {
        this.executor.execute(() -> {
            try (TempAuth auth = TempAuth.asSystem()) {
                this.dockerLogBus.accept(e);
            }
        });
    }
}
