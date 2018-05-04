package edu.scut.cs.hm.model.ngroup;

import com.fasterxml.jackson.annotation.JsonCreator;
import edu.scut.cs.hm.model.LogEvent;
import edu.scut.cs.hm.model.WithCluster;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class NodesGroupEvent extends LogEvent implements WithCluster {

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Builder extends LogEvent.Builder<Builder, NodesGroupEvent> {

        private String cluster;

        public Builder cluster(String cluster) {
            setCluster(cluster);
            return this;
        }

        @Override
        public NodesGroupEvent build() {
            return new NodesGroupEvent(this);
        }
    }

    public static final String BUS = "bus.cluman.log.nodesGroup";
    private final String cluster;

    @JsonCreator
    public NodesGroupEvent(Builder b) {
        super(b);
        this.cluster = b.cluster;
    }

    public static Builder builder() {
        return new Builder();
    }
}
