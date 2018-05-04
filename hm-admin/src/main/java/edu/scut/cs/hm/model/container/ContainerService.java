package edu.scut.cs.hm.model.container;

import com.google.common.collect.ImmutableList;
import edu.scut.cs.hm.docker.model.swarm.Service;
import edu.scut.cs.hm.docker.model.swarm.Task;
import lombok.Data;

import java.util.List;

/**
 * 'service' - in terms of docker swarm-mode. Has id, name and represent couple
 * of instances of equals containers.
 */
public class ContainerService {
    @Data
    public static class Builder {
        private String cluster;
        private Service service;
        private List<Task> tasks;

        public ContainerService build() {
            return new ContainerService(this);
        }
    }

    private final String cluster;
    private final Service service;
    private final List<Task> tasks;

    public ContainerService(Builder b) {
        this.cluster = b.cluster;
        this.service = b.service;
        this.tasks = b.tasks == null? ImmutableList.of() : ImmutableList.copyOf(b.tasks);
    }

    public static Builder builder() {
        return new Builder();
    }
}
