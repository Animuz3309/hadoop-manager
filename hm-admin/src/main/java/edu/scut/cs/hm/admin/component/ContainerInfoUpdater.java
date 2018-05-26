package edu.scut.cs.hm.admin.component;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import edu.scut.cs.hm.admin.security.TempAuth;
import edu.scut.cs.hm.admin.service.ContainerStorageImpl;
import edu.scut.cs.hm.admin.service.NodeStorage;
import edu.scut.cs.hm.common.mb.Subscriptions;
import edu.scut.cs.hm.common.utils.RescheduledTask;
import edu.scut.cs.hm.docker.DockerService;
import edu.scut.cs.hm.docker.arg.GetContainersArg;
import edu.scut.cs.hm.docker.model.container.ContainerBase;
import edu.scut.cs.hm.docker.model.container.DockerContainer;
import edu.scut.cs.hm.docker.model.events.DockerEventType;
import edu.scut.cs.hm.docker.model.events.DockerLogEvent;
import edu.scut.cs.hm.docker.model.events.DockerServiceEvent;
import edu.scut.cs.hm.model.StandardAction;
import edu.scut.cs.hm.model.container.ContainerRegistration;
import edu.scut.cs.hm.model.node.NodeEvent;
import edu.scut.cs.hm.model.node.NodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

@Slf4j
@Component
public class ContainerInfoUpdater implements SmartLifecycle {
    private final ContainerStorageImpl containerStorage;
    private final ConcurrentMap<String, RescheduledTask> scheduledNodes;
    private final ScheduledExecutorService scheduledService;
    private final NodeStorage nodeStorage;
    private boolean started;

    @Autowired
    public ContainerInfoUpdater(NodeStorage nodeStorage,
                                ContainerStorageImpl containerStorage,
                                @Qualifier(NodeEvent.BUS) Subscriptions<NodeEvent> nodeSubs,
                                @Qualifier(DockerServiceEvent.BUS) Subscriptions<DockerServiceEvent> dockerSubs,
                                @Qualifier(DockerLogEvent.BUS) Subscriptions<DockerLogEvent> dockerLogSubs) {
        this.nodeStorage = nodeStorage;
        this.containerStorage = containerStorage;
        nodeSubs.subscribe(this::onNodeEvent);
        dockerSubs.subscribe(this::onDockerEvent);
        dockerLogSubs.subscribe(this::onDockerLogEvent);
        this.scheduledService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat(getClass().getSimpleName() + "-%d")
                .build());
        this.scheduledNodes = new ConcurrentHashMap<>();
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public void start() {
        this.started = true;
    }

    @Override
    public void stop() {
        if(!this.started) {
            return;
        }
        this.started = false;
    }

    @Override
    public boolean isRunning() {
        return started;
    }

    @Override
    public int getPhase() {
        return 0;
    }

    private void onDockerLogEvent(DockerLogEvent dle) {
        if(dle.getType() != DockerEventType.CONTAINER) {
            return;
        }
        final ContainerBase container = dle.getContainer();
        if (container == null) {
            return;
        }
        final String id = container.getId();
        ContainerRegistration cr = null;
        String action = dle.getAction();
        switch(action) {
            case "delete": {
                containerStorage.deleteContainer(id);
                break;
            }
            default: {
                cr = containerStorage.getContainer(id);
            }
        }
        if(cr != null) {
            cr.modify(cb -> {
                DockerContainer.State state = container.getState();
                if(state != null) {
                    cb.setState(state);
                    // we can not retrieve status from event
                    // but old status may confuse user
                    cb.setStatus(null);
                }
            });
        }
        String node = dle.getNode();
        log.info("Schedule node '{}' update due to container '{}' changed to: {}", node, id, action);
        scheduleNodeUpdate(node);
    }

    /**
     * Schedule update of specified node in yhe next 10 seconds. Concrete time may be changed in future.
     * @param node name of node
     */
    private void scheduleNodeUpdate(String node) {
        RescheduledTask task = this.scheduledNodes.computeIfAbsent(node, (n) -> {
            Runnable runnable = () -> this.updateNodeByName(n);
            return RescheduledTask.builder()
                    .service(scheduledService)
                    .runnable(runnable)
                    .maxDelay(1L, TimeUnit.MINUTES)
                    .build();
        });
        task.schedule(10L, TimeUnit.SECONDS);
    }

    private void onDockerEvent(DockerServiceEvent e) {
        String action = e.getAction();
        if(StandardAction.UPDATE.value().equals(action) || StandardAction.OFFLINE.value().equals(action)) {
            return;
        }
        String node = e.getNode();
        if(node == null) {
            return;
        }
        log.info("Node service '{}' is {}, schedule update containers.", node, action);
        scheduleNodeUpdate(node);
    }

    private void updateNodeByName(String node) {
        try(TempAuth ta = TempAuth.asSystem()) {
            DockerService service = nodeStorage.getDockerService(node);
            if (service == null) {
                return;
            }
            // we must _not_ check service to 'online' here
            updateForNode(service);
        }
    }

    private void onNodeEvent(NodeEvent nodeEvent) {
        NodeInfo ni = nodeEvent.getNode();
        if(ni == null) {
            return;
        }
        String name = ni.getName();
        NodeEvent.Action action = nodeEvent.getAction();
        // we must keep container in all cases except deletion of node
        if(NodeEvent.Action.DELETE == action) {
            log.info("Node '{}' is '{}' remove containers.", name, action);
            containerStorage.removeNodeContainers(name);
            scheduledNodes.remove(name);
            return;
        }
        // at first event 'ONLINE', node does not have a service, but we ignore second event
        // so we need to wait when docker service is registered
        if(NodeEvent.Action.ONLINE == action) {
            DockerService dockerService = nodeStorage.getDockerService(name);
            // we do _not_ check service to 'online' here
            if(dockerService != null) {
                log.info("Node '{}' is online force update containers.", name);
                updateForNode(dockerService);
                return;
            }
        }
        //we schedule node for update, for first time only.
        // because all other event may be lost
        if(!scheduledNodes.containsKey(name)) {
            scheduleNodeUpdate(name);
        }
    }

    @Scheduled(fixedDelay = 5L * 60_000L /* 5 min */)
    public void update() {
        try(TempAuth ta = TempAuth.asSystem()) {
            log.info("Begin update containers list");
            for(String node: nodeStorage.getNodeNames()) {
                DockerService nodeService = nodeStorage.getDockerService(node);
                // we do _not_ check service to 'online' here
                if(nodeService == null) {
                    continue;
                }
                updateForNode(nodeService);
            }
            log.info("End update containers list");
        }
    }

    private void updateForNode(DockerService nodeService) {
        String node = nodeService.getNode();
        log.info("Update containers list of node '{}'", node);
        try {
            List<DockerContainer> containers = nodeService.getContainers(new GetContainersArg(true));
            Set<String> old = this.containerStorage.getContainersIdsByNode(node);
            for(DockerContainer dc: containers) {
                old.remove(dc.getId());
                this.containerStorage.updateAndGetContainer(dc, node);
            }
            this.containerStorage.remove(old);
            log.info("Containers of node '{}', current:{}, removed:{}", node, containers.size(), old.size());
        } catch (Exception e) {
            Throwable root = Throwables.getRootCause(e);
            if(root instanceof java.net.SocketException) {
                //reduce log noise when node has permanent network failure
                log.info("Updating containers of node '{}' failed with error: {}", node, root.getMessage());
            } else {
                log.info("Updating containers of node '{}' failed with error.", node, e);
            }
        }
    }
}
