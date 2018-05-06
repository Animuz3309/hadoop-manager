package edu.scut.cs.hm.docker.model.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import edu.scut.cs.hm.docker.model.container.ContainerBase;
import edu.scut.cs.hm.model.LogEvent;
import edu.scut.cs.hm.model.WithCluster;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Docker log event, <code> docker log <container> </code>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DockerLogEvent extends LogEvent implements WithCluster {

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Builder extends LogEvent.Builder<Builder, DockerLogEvent> {
        private String cluster;
        private String node;
        private ContainerBase container;
        private String status;      // Status of docker image or container.
        private DockerEventType type;

        public Builder cluster(String cluster) {
            setCluster(cluster);
            return this;
        }

        public Builder node(String node) {
            setNode(node);
            return this;
        }

        public Builder container(ContainerBase container) {
            setContainer(container);
            return this;
        }

        public Builder status(String status) {
            setStatus(status);
            return this;
        }

        public Builder type(DockerEventType type) {
            setType(type);
            return this;
        }

        @Override
        public DockerLogEvent build() {
            return new DockerLogEvent(this);
        }
    }

    public static final String BUS = "bus.hm.log.docker";

    private final String cluster;
    private final String node;
    private final ContainerBase container;
    private final String status;            // Status of docker image or container.
    private final DockerEventType type;

    @JsonCreator
    public DockerLogEvent(Builder b) {
        super(b);
        this.cluster = b.cluster;
        this.node = b.node;
        this.container = b.container;
        this.status = b.status;
        this.type = b.type;
    }

    public static Builder builder() {
        return new Builder();
    }
}
