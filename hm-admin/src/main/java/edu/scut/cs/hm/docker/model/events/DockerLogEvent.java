package edu.scut.cs.hm.docker.model.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import edu.scut.cs.hm.model.LogEvent;
import edu.scut.cs.hm.model.WithCluster;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Docker log event, <code> docker log <container> </code>
 */
// TODO No Container support
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DockerLogEvent extends LogEvent implements WithCluster {

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Builder extends LogEvent.Builder<Builder, DockerLogEvent> {
        private String cluster;
        private String node;
        private String status;
        private DockerEventType type;

        public Builder type(DockerEventType type) {
            setType(type);
            return this;
        }

        public Builder cluster(String cluster) {
            setCluster(cluster);
            return this;
        }

        public Builder node(String node) {
            setNode(node);
            return this;
        }

        public Builder status(String status) {
            setStatus(status);
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
    private final String status;
    private final DockerEventType type;

    @JsonCreator
    public DockerLogEvent(Builder b) {
        super(b);
        this.type = b.type;
        this.cluster = b.cluster;
        this.node = b.node;
        this.status = b.status;
    }

    public static Builder builder() {
        return new Builder();
    }
}
