package edu.scut.cs.hm.admin.service;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import edu.scut.cs.hm.admin.component.DockerServiceFactory;
import edu.scut.cs.hm.admin.component.SwarmProcesses;
import edu.scut.cs.hm.admin.config.configurer.DockerConfigurer;
import edu.scut.cs.hm.admin.security.TempAuth;
import edu.scut.cs.hm.common.mb.MessageBus;
import edu.scut.cs.hm.common.utils.Throwables;
import edu.scut.cs.hm.docker.DockerConfig;
import edu.scut.cs.hm.docker.DockerService;
import edu.scut.cs.hm.docker.DockerServiceImpl;
import edu.scut.cs.hm.docker.model.events.DockerServiceEvent;
import edu.scut.cs.hm.model.StandardAction;
import edu.scut.cs.hm.model.ngroup.NodesGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PreDestroy;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Registry for docker service.
 * Get Docker service from 'cluster'(united by swarm) or 'node'
 * It hold and provide swarm and docker services. It does not provide virtual services,
 * therefore you must use {@link NodesGroup#getDocker()} directly. <p/>
 */
@Slf4j
@Component
public class DockerServices {
    private final ConcurrentMap<String, DockerService> clusters = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduledExecutor;
    private final SwarmProcesses swarmProcesses;
    private final NodeStorage nodeStorage;

    private final MessageBus<DockerServiceEvent> dockerServiceEventMessageBus;
    private final DockerServiceFactory dockerFactory;

    @Autowired
    public DockerServices(DockerConfigurer.DockerServicesConfig config,
                          SwarmProcesses swarmProcesses,
                          NodeStorage nodeStorage,
                          DockerServiceFactory dockerFactory,
                          @Qualifier(DockerServiceEvent.BUS) MessageBus<DockerServiceEvent> dockerServiceEventMessageBus) {
        this.swarmProcesses = swarmProcesses;
        this.nodeStorage = nodeStorage;
        this.dockerServiceEventMessageBus = dockerServiceEventMessageBus;
        this.dockerFactory = dockerFactory;

        String classPrefix = getClass().getSimpleName();

        scheduledExecutor = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat(classPrefix + "-scheduled-%d")
                .setUncaughtExceptionHandler(Throwables.uncaughtHandler(log))
                .build());
        scheduledExecutor.scheduleWithFixedDelay(this::updateInfo,
                config.getRefreshInfoSeconds(),
                config.getRefreshInfoSeconds(),
                TimeUnit.SECONDS);

    }

    private void updateInfo() {
        // we call get info for periodically updating info cache,
        //  it need for actual info about nodes health, that we can only obtain trough swarm service
        try (TempAuth ta = TempAuth.asSystem()) {
            for (DockerService service : this.clusters.values()) {
                try {
                    service.getInfo();
                } catch (Exception e) {
                    log.error("While getInfo on {} ", service.getId(), e);
                }
            }
        }
    }

    /**
     * @see DockerService#getId()
     * @param id
     * @return
     */
    public DockerService getById(String id) {
        if(id == null || !id.startsWith(DockerService.DS_PREFIX)) {
            return null;
        }
        int off = DockerService.DS_PREFIX.length();
        int split = id.indexOf(':', off);
        String type = id.substring(off, split);
        String val = id.substring(split + 1);
        if("cluster".equals(type)) {
            return getService(val);
        }
        if("node".equals(type)) {
            return nodeStorage.getDockerService(val);
        }
        //unknown type
        return null;
    }

    /**
     * Do not use this for obtain cluster service.
     * @param instanceId id of swarm or docker node
     * @return docker service
     */
    public DockerService getService(String instanceId) {
        return clusters.get(instanceId);
    }

    /**
     * Create
     * @param dockerConfig
     * @param dockerComsumer
     * @return
     */
    public DockerService getOrCreateDocker(DockerConfig dockerConfig, Consumer<DockerServiceImpl.Builder> dockerComsumer) {
        String cluster = dockerConfig.getCluster();
        Assert.hasText(cluster, "Cluster field in config is null or empty");
        return clusters.computeIfAbsent(cluster, (cid) -> {
            DockerConfig config = dockerConfig;
            if (config.getHost() == null) {
                // if no defined swarm hosts then we must create own swarm instance and run it
                SwarmProcesses.SwarmProcess process = swarmProcesses.addCluster(dockerConfig);
                process.waitStart();

                // so, we create new swarm process and now need to modify config with process address
                DockerConfig.Builder ccib = DockerConfig.builder(dockerConfig);
                ccib.host(process.getAddress());
                config = ccib.build();
            }
            DockerService dockerService = dockerFactory.createDockerService(config, nodeStorage, dockerComsumer);
            dockerServiceEventMessageBus.accept(new DockerServiceEvent(dockerService, StandardAction.START.value()));
            return dockerService;
        });
    }

    public Set<String> getServices() {
        return ImmutableSet.copyOf(clusters.keySet());
    }

    @PreDestroy
    public void shutdown() {
        scheduledExecutor.shutdown();
    }
}
