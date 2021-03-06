package edu.scut.cs.hm.model.node;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import edu.scut.cs.hm.admin.component.PersistentBusFactory;
import edu.scut.cs.hm.docker.DockerService;
import edu.scut.cs.hm.admin.security.AccessContextFactory;
import edu.scut.cs.hm.admin.security.SecuredType;
import edu.scut.cs.hm.admin.security.TempAuth;
import edu.scut.cs.hm.admin.service.NodeStorage;
import edu.scut.cs.hm.common.mb.MessageBus;
import edu.scut.cs.hm.common.mb.MessageBuses;
import edu.scut.cs.hm.common.mb.Subscriptions;
import edu.scut.cs.hm.common.security.acl.dto.Action;
import edu.scut.cs.hm.docker.arg.GetEventsArg;
import edu.scut.cs.hm.docker.model.container.ContainerBase;
import edu.scut.cs.hm.docker.model.container.DockerContainer;
import edu.scut.cs.hm.docker.model.events.*;
import edu.scut.cs.hm.model.Severity;
import edu.scut.cs.hm.model.StandardAction;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Node registration implementation
 * @see NodeRegistration
 */
@Slf4j
@Data
public class NodeRegistrationImpl implements NodeRegistration, AutoCloseable {

    private final String name;
    private final NodeStorage nodeStorage;
    private final ObjectIdentity oid;
    private final MessageBus<NodeHealthEvent> healthBus;
    private final NodeInfoImpl.Builder builder;             // NodeInfo
    private final ScheduledExecutorService logFetcher;
    private final Object lock = new Object();

    private volatile NodeInfoImpl cache;
    private volatile int ttl;
    private volatile DockerService docker;
    private volatile ScheduledFuture<?> logFuture;  // get log from docker

    public NodeRegistrationImpl(NodeStorage nodeStorage, PersistentBusFactory pbf, NodeInfo nodeInfo) {
        String name = nodeInfo.getName();
        NodeUtils.checkName(name);
        this.name = name;
        this.nodeStorage = nodeStorage;
        this.oid = SecuredType.NODE.id(name);
        // name may contain dots
        this.healthBus = pbf.create(NodeHealthEvent.class, "node[" + name + "].metrics", 2000/* TODO in config */);
        synchronized (lock) {
            this.builder = NodeInfoImpl.builder(nodeInfo);
        }
        this.logFetcher = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("node-" + name + "-log-fetcher-%d")
                .setUncaughtExceptionHandler((thread, ex) -> log.error("Uncaught exception in '" + name + "' node log fetcher.", ex))
                .build());
    }

    public void init() {
        renewDocker();
    }

    /**
     * Time while node is actual, in seconds.
     * @param ttl time in seconds
     */
    public void setTtl(int ttl) {
        final int min = nodeStorage.getNodeStorageConfig().getMinTtl();
        if(ttl < min) {
            ttl = min;
        }
        synchronized (lock) {
            this.ttl = ttl;
        }
    }

    public int getTtl() {
        synchronized (lock) {
            return this.ttl;
        }
    }

    private void renewDocker() {
        unsubscribe();
        docker = null;
        if (builder.getAddress() != null) {
            docker = nodeStorage.createNodeDockerService(this);
            subscribe();
        }
    }

    private void subscribe() {
        DockerEventConfig cfg = nodeStorage.getDockerEventConfig();
        final int periodInSeconds = cfg.getPeriodInSeconds();
        log.info("Register log fetcher from {} node, repeat every {} seconds", name, periodInSeconds);
        Assert.isNull(this.logFuture, "Future of docker logging is not null");
        logFuture = logFetcher.scheduleAtFixedRate(
                () -> {
                        try {
                            Long time = System.currentTimeMillis();
                            Long afterTime = time + periodInSeconds * 1000L;
                            GetEventsArg getEventsArg = GetEventsArg.builder()
                                    .since(time)
                                    .until(afterTime)
                                    .watcher(this::proxyDockerEvent)
                                    .build();
                            log.debug("getting event args {}", getEventsArg);
                            try (TempAuth auth = TempAuth.asSystem()) {
                                docker.subscribeToEvents(getEventsArg);
                            }
                        } catch (Exception e) {
                            log.error("Can not fetch logs from {}, try again after {} seconds, error: {}", name, periodInSeconds, e.toString());
                        }
                    },
                cfg.getInitialDelayInSeconds(),
                periodInSeconds, TimeUnit.SECONDS);
    }

    private void proxyDockerEvent(DockerEvent e) {
        try {
            log.debug("Node '{}' send log event: {}", name, e);
            DockerLogEvent logEvent = convertToLogEvent(e);
            nodeStorage.acceptDockerLogEvent(logEvent);
        } catch (Exception ex) {
            log.error("can not convert {}", e, ex);
        }
    }

    private DockerLogEvent convertToLogEvent(final DockerEvent e) {
        DockerLogEvent.Builder logEvent = DockerLogEvent.builder();
        final String action = e.getAction();
        logEvent.action(action);
        // be careful
        String localNodeName = (e.getNode() != null) ? e.getNode().getName() : this.name;
        final DockerEventType type = e.getType();
        if (type == DockerEventType.CONTAINER) {
            ContainerBase.Builder builder = ContainerBase.builder();
            builder.setId(e.getId());
            builder.setNode(localNodeName);
            Actor actor = e.getActor();
            Map<String, String> attributes = actor.getAttributes();
            builder.setLabels(attributes);
            //remove attributes which is not a labels.
            builder.getLabels().keySet().removeAll(ImmutableSet.of("name", "image"));
            builder.setName(attributes.get("name"));
            builder.setImage(e.getFrom());
            DockerContainer.State state = null;
            // Containers report these events: attach, commit, copy, create, destroy, detach, die, exec_create,
            // exec_detach, exec_start, export, kill, oom, pause, rename, resize, restart, start,
            // stop, top, unpause, update
            switch (action) {
                //we do not support 'kill' action
                case "kill":
                    logEvent.setAction(StandardAction.STOP.value());
                    state = DockerContainer.State.EXITED;
                    break;
                case "destroy":
                    logEvent.setAction(StandardAction.DELETE.value());
                    state = DockerContainer.State.REMOVING;
                    break;
                case "die":
                case "stop":
                    state = DockerContainer.State.DEAD;
                    break;
                case "unpause":
                case "start":
                    state = DockerContainer.State.RUNNING;
                    break;
                case "pause":
                    state = DockerContainer.State.PAUSED;
                    break;
                case "restart":
                    state = DockerContainer.State.RESTARTING;
                    break;
            }
            builder.setState(state);
            logEvent.setContainer(builder.build());
        }

        logEvent.setDate(new Date(e.getTime() * 1000L));
        logEvent.setNode(localNodeName);
        String eventCluster = nodeStorage.getNodeCluster(localNodeName);
        String thisCluster = getCluster();
        if (!Objects.equals(thisCluster, eventCluster)) {
            log.warn("Current node cluster '{}' differ from event cluster '{}'", thisCluster, eventCluster);
        }
        logEvent.setCluster(eventCluster);
        logEvent.setType(type);
        logEvent.setStatus(e.getStatus());
        logEvent.setSeverity(calculateSeverity(e.getStatus()));
        return logEvent.build();
    }

    private Severity calculateSeverity(String status) {
        if(status == null) {
            return Severity.INFO;
        }
        switch (status) {
            case "die":
                return Severity.ERROR;
            case "kill":
                return Severity.WARNING;
            default:
                return Severity.INFO;
        }
    }

    @Override
    public Subscriptions<NodeHealthEvent> getHealthSubscriptions() {
        return this.healthBus.asSubscriptions();
    }

    @Override
    public NodeInfoImpl getNodeInfo() {
        NodeInfoImpl ni;
        NodeInfoImpl old;
        final boolean onlineChanged;
        synchronized (lock) {
            builder.name(name);
            boolean on = isOn();
            onlineChanged = on != builder.isOn();
            old = ni = cache;
            if(ni == null || onlineChanged) {
                ni = cache = builder.on(on).build();
            }
        }
        if(onlineChanged) {
            fireNodeChanged(ni.isOn() ? NodeEvent.Action.ONLINE : NodeEvent.Action.OFFLINE, old, ni);
        }
        return ni;
    }

    private boolean isOn() {
        DockerService service = this.getDocker();
        return service != null && service.isOnline();
    }

    private void fireNodeChanged(NodeEvent.Action action, NodeInfoImpl old, NodeInfoImpl ni) {
        this.nodeStorage.fireNodeModification(this, action, old, ni);
    }

    public void updateHealth(NodeMetrics metrics) {
        checkAccessUpdate();
        NodeMetrics newMetrics;
        String cluster;
        synchronized (lock) {
            newMetrics = NodeMetrics.builder().from(builder.getHealth()).fromNonNull(metrics).build();
            builder.setHealth(newMetrics);
            cluster = builder.getCluster();
            cache = null;
        }
        healthBus.accept(new NodeHealthEvent(name, cluster, newMetrics));
    }

    public void updateNodeInfo(Consumer<NodeInfoImpl.Builder> modifier) {
        checkAccessUpdate();
        NodeMetrics nmnew = null;
        String cluster;
        NodeInfoImpl oldni;
        NodeInfoImpl ni;
        synchronized (lock) {
            oldni = getNodeInfo();
            NodeMetrics oldMetrics = builder.getHealth();
            boolean on = builder.isOn();
            NodeInfoImpl.Builder copy = NodeInfoImpl.builder(builder);
            modifier.accept(copy);
            validate(copy);
            boolean cancel = nodeStorage.fireNodePreModification(oldni, copy.build());
            if(cancel) {
                return;
            }

            builder.from(copy);

            NodeMetrics newMetrics = builder.getHealth();
            if (!Objects.equals(oldMetrics, newMetrics)) {
                nmnew = newMetrics;
            }
            builder.on(on); // we must save on flag
            //refresh address if need
            this.setAddress(this.builder.getAddress());
            cluster = this.builder.getCluster();
            cache = null;
            ni = getNodeInfo();
        }
        if(!Objects.equals(oldni, ni)) {//we try to reduce count of unnecessary 'update' event
            fireNodeChanged(NodeEvent.Action.UPDATE, oldni, ni);
        }
        if(nmnew != null) {
            this.healthBus.accept(new NodeHealthEvent(this.name, cluster, nmnew));
        }
    }

    private void validate(NodeInfoImpl.Builder copy) {
        Assert.isTrue(Objects.equals(copy.getName(), this.name),
                "Wrong name of modified node: " + copy.getName() + ", when must be: " + this.name);
    }

    private void checkAccessUpdate() {
        AccessContextFactory.getLocalContext().assertGranted(oid, Action.UPDATE);
    }

    public void setCluster(String cluster) {
        update(this.builder::getCluster, this.builder::setCluster, cluster);
    }

    private <T> void update(Supplier<T> getter, Consumer<T> setter, T value) {
        NodeInfoImpl ni = null;
        NodeInfoImpl oldInfo;
        synchronized (lock) {
            oldInfo = cache;
            T oldVal = getter.get();
            if(!Objects.equals(oldVal, value)) {
                setter.accept(value);
                cache = null;
                ni = getNodeInfo();
                boolean cancel = true;
                try {
                    cancel = nodeStorage.fireNodePreModification(oldInfo, ni);
                } finally {
                    if(cancel) {
                        // it not good practice, and require that setter must be 'safe'
                        setter.accept(oldVal);
                        cache = null;
                        ni = null;
                    }
                }
            }
        }
        if(ni != null) {
            fireNodeChanged(NodeEvent.Action.UPDATE, oldInfo, ni);
        }
    }

    @Override
    public ObjectIdentity getOid() {
        return oid;
    }

    @Override
    public DockerService getDocker() {
        synchronized (lock) {
            return docker;
        }
    }

    /**
     * It change address of node, that cause some side effects: recreation of DockerService for example.
     * @param address new address of node or null
     * @return new docker service, or old when address same as old
     */
    public DockerService setAddress(String address) {
        synchronized (lock) {
            update(this.builder::getAddress, this.builder::setAddress, address);
            if(docker != null && docker.getAddress().equals(address)) {
                return getDocker();
            }
            renewDocker();
            return getDocker();
        }
    }

    @Override
    public void close() throws Exception {
        unsubscribe();
        this.logFetcher.shutdownNow();
    }

    private void unsubscribe() {
        ScheduledFuture<?> future = this.logFuture;
        this.logFuture = null;
        if(future != null) {
            future.cancel(true);
        }
    }

    public String getAddress() {
        synchronized (lock) {
            return builder.getAddress();
        }
    }

    @Override
    public String getCluster() {
        synchronized (lock) {
            return this.builder.getCluster();
        }
    }
}
