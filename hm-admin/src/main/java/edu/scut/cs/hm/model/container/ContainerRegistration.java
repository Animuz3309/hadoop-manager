package edu.scut.cs.hm.model.container;

import edu.scut.cs.hm.admin.service.ContainerStorageImpl;
import edu.scut.cs.hm.common.kv.mapping.KvMap;
import edu.scut.cs.hm.common.kv.mapping.KvMapping;
import edu.scut.cs.hm.common.utils.RescheduledTask;
import edu.scut.cs.hm.docker.model.container.DockerContainer;
import edu.scut.cs.hm.model.ContainerBaseIface;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * A obj(represent container) to store in k-v storage
 */
public class ContainerRegistration {
    private final String id;
    private final RescheduledTask resheduleTask;
    @KvMapping
    private Map<String, String> additionalLabels;
    /**
     * We persist container for detect cases when it quietly removed
     */
    @KvMapping
    private DockerContainer.Builder container;
    private final Object lock = new Object();
    private DockerContainer cached;
    private KvMap<?> map;

    public ContainerRegistration(ContainerStorageImpl csi, String id) {
        this.id = id;
        Assert.notNull(id, "id is null");
        this.container = DockerContainer.builder().id(id);
        this.map = csi.getMap();
        this.resheduleTask = RescheduledTask.builder()
                .maxDelay(10L, TimeUnit.SECONDS)
                .service(csi.getExecutorService())
                .runnable(this::flush)
                .build();
    }

    public String getId() {
        return id;
    }

    public void setAdditionalLabels(Map<String, String> additionalLabels) {
        this.additionalLabels = additionalLabels;
    }

    public Map<String, String> getAdditionalLabels() {
        return additionalLabels == null? Collections.emptyMap() : Collections.unmodifiableMap(additionalLabels);
    }

    public void scheduleFlush() {
        this.resheduleTask.schedule(10L, TimeUnit.SECONDS);
    }
    public void flush() {
        map.flush(id);
    }

    /**
     * Return container from its registration, when container invalid - return null.
     * @return null when container is invalid
     */
    public DockerContainer getContainer() {
        DockerContainer dc = cached;
        if(dc == null) {
            synchronized (lock) {
                dc = cached = container.id(id).build();
            }
        }
        return dc;
    }

    public String getNode() {
        synchronized (lock) {
            return this.container.getNode();
        }
    }

    /**
     * Note that this method do 'flush' in different thread.
     * @param modifier callback which can modify container, and must not block thread.
     */
    public void modify(Consumer<DockerContainer.Builder> modifier) {
        synchronized (lock) {
            modifier.accept(this.container);
            validate();
            this.cached = null;
        }
        scheduleFlush();
    }

    public void from(ContainerBaseIface container, String node) {
        modify((cb) -> {
            synchronized (lock) {
                this.container.from(container).setNode(node);
            }
        });
    }

    private void validate() {
        String name = this.container.getName();
        // swarm can give container names with leading '/'
        if(name != null && name.startsWith("/")) {
            throw new IllegalArgumentException("Bad container name: " + name);
        }
        String currId = this.container.getId();
        Assert.isTrue(this.id.equals(currId), "After update container has differ id: old=" + this.id + " new=" + currId);
    }

    public String forLog() {
        synchronized (lock) {
            return new StringBuilder().append(id).append(" \'")
                    .append(container.getName()).append("\' of \'")
                    .append(container.getImage()).append('\'')
                    .toString();
        }
    }

    public void close() {
        resheduleTask.close();
    }
}
