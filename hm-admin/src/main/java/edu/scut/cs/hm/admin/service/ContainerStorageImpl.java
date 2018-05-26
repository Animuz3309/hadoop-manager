package edu.scut.cs.hm.admin.service;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import edu.scut.cs.hm.common.kv.mapping.KvMap;
import edu.scut.cs.hm.common.kv.mapping.KvMapperFactory;
import edu.scut.cs.hm.common.utils.Throwables;
import edu.scut.cs.hm.docker.model.container.DockerContainer;
import edu.scut.cs.hm.model.ContainerBaseIface;
import edu.scut.cs.hm.model.container.ContainerRegistration;
import edu.scut.cs.hm.model.container.ContainerStorage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Storage service for Containers
 */
@Slf4j
@Component
@Getter
public class ContainerStorageImpl implements ContainerStorage {
    private final KvMap<ContainerRegistration> map;
    private final ScheduledExecutorService executorService;

    @Autowired
    public ContainerStorageImpl(KvMapperFactory kvmf) {
        String prefix = kvmf.getStorage().getPrefix() + "/containers/";
        this.map = KvMap.builder(ContainerRegistration.class)
                .mapper(kvmf)
                .path(prefix)
                .factory((key, type) -> new ContainerRegistration(this, key))
                .build();
        this.executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat(getClass().getSimpleName() + "-scheduled-%d")
                .setUncaughtExceptionHandler(Throwables.uncaughtHandler(log))
                .build());
    }

    @PostConstruct
    public void postConstruct() {
        this.map.load();
    }

    @PreDestroy
    private void preDestroy() {
        this.executorService.shutdown();
    }

    @Override
    public void deleteContainer(String id) {
        ContainerRegistration cr = map.remove(id);
        if(cr != null) {
            log.info("Container remove: {} ", cr.forLog());
            cr.close();
        }
    }

    @Override
    public List<ContainerRegistration> getContainers() {
        return ImmutableList.copyOf(map.values());
    }

    @Override
    public ContainerRegistration getContainer(String id) {
        return map.get(id);
    }

    @Override
    public ContainerRegistration findContainer(String name) {
        ContainerRegistration cr = map.get(name);
        if(cr == null) {
            cr = map.values().stream().filter((item) -> {
                DockerContainer container = item.getContainer();
                return container != null && (item.getId().startsWith(name) || Objects.equals(container.getName(), name));
            }).findAny().orElse(null);
        }
        return cr;
    }

    @Override
    public List<ContainerRegistration> getContainersByNode(String nodeName) {
        return containersByNode(nodeName)
                .collect(Collectors.toList());
    }

    private Stream<ContainerRegistration> containersByNode(String nodeName) {
        return map.values()
                .stream()
                .filter(c -> Objects.equals(c.getNode(), nodeName));
    }

    public Set<String> getContainersIdsByNode(String nodeName) {
        return containersByNode(nodeName)
                .map(ContainerRegistration::getId)
                .collect(Collectors.toSet());
    }

    /**
     * Get or create container.
     *
     * @param container
     * @param node
     * @return
     */
    @Override
    public ContainerRegistration updateAndGetContainer(ContainerBaseIface container, String node) {
        ContainerRegistration cr = map.computeIfAbsent(container.getId(), s -> new ContainerRegistration(this, s));
        cr.from(container, node);
        log.info("Update container: {}", cr.forLog());
        return cr;
    }

    public void remove(Set<String> ids) {
        ids.forEach(this::deleteContainer);
    }

    public void removeNodeContainers(String nodeName) {
        Set<String> nodeIds = getContainersIdsByNode(nodeName);
        remove(nodeIds);
    }
}
