package edu.scut.cs.hm.model.cluster;

import com.google.common.collect.ImmutableSet;
import edu.scut.cs.hm.admin.security.SecuredType;
import edu.scut.cs.hm.admin.security.TempAuth;
import edu.scut.cs.hm.admin.security.acl.AclModifier;
import edu.scut.cs.hm.admin.service.NodeService;
import edu.scut.cs.hm.common.security.SecurityUtils;
import edu.scut.cs.hm.common.security.acl.TenantPrincipalSid;
import edu.scut.cs.hm.common.security.acl.dto.AclSource;
import edu.scut.cs.hm.common.security.acl.dto.ObjectIdentityData;
import edu.scut.cs.hm.docker.DockerService;
import edu.scut.cs.hm.docker.model.network.Network;
import edu.scut.cs.hm.docker.model.swarm.NetworkManager;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * Abstract of nodes group
 * @param <C>
 */
@ToString
public abstract class AbstractNodesGroup<C extends AbstractNodesGroupConfig<C>> implements NodesGroup, AutoCloseable {

    private static final int S_BEGIN = 0;
    private static final int S_INITING = 1;
    private static final int S_INITED = 2;
    private static final int S_CLENING = 3;
    private static final int S_CLENED = 4;
    private static final int S_FAILED = 99;

    protected final Object lock = new Object();
    protected final NetworkManager networkManager;
    protected volatile C config;

    private final Logger log = LoggerFactory.getLogger(getClass());     // not use static or @Sl4j here, because need use log(subclass)
    private final CreateNetworkTask createNetworkTask = new CreateNetworkTask();
    private final AtomicInteger state = new AtomicInteger(S_BEGIN);     // state of nodes group
    private volatile String stateMessage;                               // state msg
    private ClusterService service;                                     // service to do with nodes group
    private Class<C> configClass;                                       // class type of NodesGroupConfig
    private String name;                                                // name of nodes group
    private ObjectIdentityData oid;                                     // oid of nodes group
    private Set<Feature> features;                                      // feature of nodes group

    @SuppressWarnings("unchecked")
    public AbstractNodesGroup(C config, ClusterService service, Collection<Feature> features) {
        this.service = service;
        Assert.notNull(this.service, "cluster service is null");
        this.configClass = (Class<C>) config.getClass();
        this.name = config.getName();
        Assert.notNull(this.name, "name is null");
        this.oid = SecuredType.CLUSTER.id(name);
        this.features = features == null ? Collections.emptySet() : ImmutableSet.copyOf(features);
        setConfig(config);
        this.networkManager = new NetworkManager(this);
    }

    /**
     * Try to init cluster if it not inited already
     * @see #getState()
     */
    @Override
    public final void init() {
        if (!changeState(S_BEGIN, S_INITING)) {
            return;
        }
        try {
            log.info("Begin init of cluster '{}'", getName());
            initImpl();
            if (changeState(S_INITING, S_CLENED)) {
                log.info("Success init of cluster '{}'", getName());
            }
        } finally {
            if (changeState(S_INITING, S_FAILED)) {
                // NOTE: if cluster may be re-inited then initImpl() MUST:
                // - properly handle any errors
                // - set status to S_BEGIN after errors which prevent correct initialisation (like node is offline)
                // otherwise cluster will gone to failed state, which is unrecoverable
                log.error("Fail to init of cluster '{}'", getName());
            }
        }
    }

    protected void initImpl() {
        // none
    }

    protected final void cancelInit(String msg) {
        log.warn("Init of {} cluster cancelled due to: {}", getName(), msg);
        changeState(S_INITING, S_BEGIN, msg);
    }

    @Override
    public void close() {
        closeImpl();
    }

    protected void closeImpl() {
        // none
    }

    @Override
    public void clean() {
        if (changeState(S_INITED, S_CLENING)) {
            try {
                cleanImpl();
            } finally {
                changeState(S_CLENING, S_CLENED);
            }
        }
    }

    protected void cleanImpl() {
        // none
    }

    protected int getStateCode() {
        return state.get();
    }

    private boolean changeState(int oldState, int newState) {
        return changeState(oldState, newState, null);
    }

    private boolean changeState(int oldState, int newState, String stateMessage) {
        boolean res = state.compareAndSet(oldState, newState);
        if (res) {
            this.stateMessage = stateMessage;
        }
        return res;
    }

    @Override
    public void flush() {
        // todo with ClusterService
    }

    /**
     * Return clone of this.config
     * @return
     */
    @Override
    public C getConfig() {
        synchronized (lock) {
            return config.clone();
        }
    }

    /**
     * Clone config to this.config
     * @param config
     */
    @SuppressWarnings("unchecked")
    @Override
    public void setConfig(AbstractNodesGroupConfig<?> config) {
        this.configClass.cast(config);
        validateConfig(config);
        synchronized (lock) {
            this.config = (C) config.clone();
            onConfig();
        }
        flush();
    }

    /**
     * here you can handle config update, before flush
     */
    protected void onConfig() {
        // none
    }

    @Override
    public void updateConfig(Consumer<AbstractNodesGroupConfig<?>> consumer) {
        synchronized (lock) {
            C clone = config.clone();
            consumer.accept(clone);
            validateConfig(clone);
            this.config = clone;
        }
        flush();
    }

    private void validateConfig(AbstractNodesGroupConfig<?> config) {
        AclSource acl = config.getAcl();
        if (acl != null && !oid.equals(acl.getObjectIdentity())) {
            throw new IllegalArgumentException("Bad acl.objectIdentity in config: " + config);
        }
    }

    @Override
    public NodeGroupState getState() {
        NodeGroupState.Builder b = NodeGroupState.builder();
        String msg = this.stateMessage;
        b.inited(true);
        switch (state.get()) {
            case S_BEGIN:
            case S_INITING:
                b.message("Not inited.").ok(false).inited(false);
                break;
            case S_INITED:
                b.message("Inited.").ok(true);                      // the only state is OK
                break;
            case S_CLENING:
            case S_CLENED:
                b.message("Cleaning or cleaned").ok(false);
                break;
            case S_FAILED:
                b.message("Failed.").ok(false);
                break;
            default:
                b.message("Unknown state.").ok(false);
        }
        if(msg != null) {
            b.message(msg);
        }
        return b.build();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTitle() {
        synchronized (lock) {
            return config.getTitle();
        }
    }

    @Override
    public String getImageFilter() {
        synchronized (lock) {
            return config.getImageFilter();
        }
    }

    @Override
    public void setImageFilter(String imageFilter) {
        synchronized (lock) {
            config.setImageFilter(imageFilter);
        }
    }

    @Override
    public String getDescription() {
        synchronized (lock) {
            return config.getDescription();
        }
    }

    @Override
    public void setDescrition(String descrition) {
        synchronized (lock) {
            config.setDescription(descrition);
        }
    }

    @Override
    public Set<Feature> getFeatures() {
        return features;
    }

    @Override
    public NetworkManager getNetworks() {
        return networkManager;
    }

    @Override
    public String getDefaultNetworkName() {
        String defaultNetwork;
        synchronized (lock) {
            defaultNetwork = config.getDefaultNetwork();
            if(defaultNetwork == null) {
                defaultNetwork = getName();
            }
        }
        return defaultNetwork;
    }

    protected void createDefaultNetwork() {
        NodeGroupState state = getState();
        if (!state.isOk()) {
            log.warn("Can not create network due cluster '{}' in '{}' state.", getName(), state.getMessage());
            return;
        }
        getClusterService().getExecutor().execute(createNetworkTask);
    }

    @Override
    public ObjectIdentity getOid() {
        return oid;
    }

    @Override
    public AclSource getAcl() {
        synchronized (lock) {
            AclSource acl = config.getAcl();
            if (acl == null) {
                // we must not return null, but also can not update config here, so make default non null value
                acl = defaultAclBuilder().build();
            }
            return acl;
        }
    }

    private AclSource.Builder defaultAclBuilder() {
        return AclSource.builder()
                .owner(TenantPrincipalSid.from(SecurityUtils.USER_SYSTEM))
                .objectIdentity(oid);
    }

    @Override
    public void updateAcl(AclModifier operator) {
        synchronized (lock) {
            AclSource acl = config.getAcl();
            AclSource.Builder b = defaultAclBuilder().from(acl);
            if (!operator.modify(b)) {
                return;
            }

            // we set true oid before modification for using in modifier, but not allow modifier to change it
            if (!oid.equals(b.getObjectIdentity())) {
                throw new IllegalArgumentException("Invalid oid of updated acl: " + b);
            }
            AclSource modified = b.build();
            config.setAcl(modified);
        }
        flush();
    }

    protected ClusterService getClusterService() {
        return service;
    }

    protected NodeService getNodeService() {
        return service.getNodeService();
    }

    private class CreateNetworkTask implements Runnable {
        private final Lock lock = new ReentrantLock();

        @Override
        public void run() {
            if(!lock.tryLock()) {
                // this case actual when one of tasks already in execution
                return;
            }
            try (TempAuth ta = TempAuth.asSystem()) {
                DockerService docker = getDocker();
                if(docker == null || !docker.isOnline()) {
                    log.warn("Can not create networks in '{}' cluster due to null or offline docker", getName());
                    return;
                }

                List<Network> networks = docker.getNetworks();
                log.debug("Networks {}", networks);
                String defaultNetwork = getDefaultNetworkName();
                Optional<Network> any = networks.stream().filter(n -> n.getName().equals(defaultNetwork)).findAny();
                if (any.isPresent()) {
                    return;
                }
                networkManager.createNetwork(defaultNetwork);
            } finally {
                lock.unlock();
            }
        }
    }
}
