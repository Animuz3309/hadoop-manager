package edu.scut.cs.hm.model.container;

import edu.scut.cs.hm.admin.component.ContainerCreator;
import edu.scut.cs.hm.common.utils.SingleValueCache;
import edu.scut.cs.hm.docker.model.swarm.Task;
import edu.scut.cs.hm.model.cluster.DockerCluster;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Containers manager for swarm-mode clusters. <p/>
 * We must prevent managing of containers which is enclosed to existed 'service'.
 */
@Slf4j
public class DockerClusterContainers implements ContainersManager {
    protected final DockerCluster dc;
    protected final ContainerStorage containerStorage;
    protected final SingleValueCache<Map<String, ContainerService>> svcmap;
    protected final SingleValueCache<Map<String, List<Task>>> tasksmap;
    private final ContainerCreator containerCreator;

    public DockerClusterContainers(DockerCluster dc, ContainerStorage containerStorage, ContainerCreator containerCreator) {
        this.dc = dc;
        this.containerStorage = containerStorage;
        this.containerCreator = containerCreator;
        this.svcmap = SingleValueCache.builder(this::loadServices)
                .timeAfterWrite(TimeUnit.SECONDS, dc.getConfig().getConfig().getCacheTimeAfterWrite())
                .build();
        this.tasksmap = SingleValueCache.builder(this::loadTasks)
                .timeAfterWrite(TimeUnit.SECONDS, dc.getConfig().getConfig().getCacheTimeAfterWrite())
                .build();
    }

    private Map<String, List<Task>> loadTasks() {
        //todo
        return null;
    }

    private Map<String, ContainerService> loadServices() {
        //todo
        return null;
    }
}
