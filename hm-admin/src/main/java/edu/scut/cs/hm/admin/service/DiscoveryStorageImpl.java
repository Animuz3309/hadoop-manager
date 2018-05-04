package edu.scut.cs.hm.admin.service;

import edu.scut.cs.hm.admin.component.FilterFactory;
import edu.scut.cs.hm.admin.security.AccessContextFactory;
import edu.scut.cs.hm.admin.security.TempAuth;
import edu.scut.cs.hm.common.kv.KeyValueStorage;
import edu.scut.cs.hm.common.kv.mapping.KvMap;
import edu.scut.cs.hm.common.kv.mapping.KvMapAdapter;
import edu.scut.cs.hm.common.kv.mapping.KvMapLocalEvent;
import edu.scut.cs.hm.common.kv.mapping.KvMapperFactory;
import edu.scut.cs.hm.common.mb.MessageBus;
import edu.scut.cs.hm.docker.DockerService;
import edu.scut.cs.hm.model.StandardAction;
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

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * Cluster discovery storage implementation
 */
@Slf4j
@Primary
@Component
public class DiscoveryStorageImpl implements DiscoveryStorage {

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
    }

    /**
     * Return exists ngroup of concrete node.
     *
     * @param node
     * @return ngroup or null
     */
    @Override
    public NodesGroup getClusterForNode(String node) {
        return null;
    }

    /**
     * Return exists ngroup or null
     *
     * @param clusterId
     * @return exists ngroup or null
     */
    @Override
    public NodesGroup getCluster(String clusterId) {
        return null;
    }

    /**
     * Return existed ngroup or create new.
     *
     * @param clusterId name of ngroup
     * @param factory   factory or null
     * @return NodesGroup, never null
     */
    @Override
    public NodesGroup getOrCreateCluster(String clusterId, ClusterConfigFactory factory) {
        return null;
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
        return null;
    }

    /**
     * Delete ngroup by ngroup name (real ngroup)
     *
     * @param clusterId ngroup name {@link NodesGroup#getName()}
     */
    @Override
    public void deleteCluster(String clusterId) {

    }

    /**
     * Delete ngroup by nodes group name (just in logic nodes group not real ngroup)
     *
     * @param clusterId nodes group name {@link NodesGroup#getName()}
     */
    @Override
    public void deleteNodeGroup(String clusterId) {

    }

    /**
     * Get all ngroup (include nodes group and real ngroup)
     *
     * @return
     */
    @Override
    public List<NodesGroup> getClusters() {
        return null;
    }

    /**
     * Docker service
     *
     * @param name
     * @return
     */
    @Override
    public DockerService getService(String name) {
        return null;
    }

    /**
     * In docker swarm mode service means a combination of containers(maybe in different nodes)
     *
     * @return
     */
    @Override
    public Set<String> getServices() {
        return null;
    }

    /**
     * Get executor
     * TODO refactor
     * @return
     */
    public ExecutorService getExecutor() {
        return null;
    }

    /**
     * Get NodeService
     * TODO refactor
     * @return
     */
    public NodeStorage getNodeStorage() {
        return null;
    }

    /**
     * Return DockerServices
     * @return
     */
    public DockerServices getDockerServices() {
        return this.dockerServices;
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
