package edu.scut.cs.hm.docker.model.swarm;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableList;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public final class SwarmInfo {

    @Data
    public static class Builder {
        private String clusterId;
        private String nodeId;
        private boolean manager;
        private final List<String> managers = new ArrayList<>();

        public Builder clusterId(String clusterId) {
            setClusterId(clusterId);
            return this;
        }

        public Builder nodeId(String nodeId) {
            setNodeId(nodeId);
            return this;
        }

        public Builder manager(boolean manager) {
            setManager(manager);
            return this;
        }

        public Builder managers(List<String> managers) {
            setManagers(managers);
            return this;
        }

        private void setManagers(List<String> managers) {
            this.managers.clear();
            if(managers != null) {
                this.managers.addAll(managers);
            }
        }

        public SwarmInfo build() {
            return new SwarmInfo(this);
        }
    }

    private final String clusterId;
    private final String nodeId;
    private final boolean manager;
    private final List<String> managers;

    @JsonCreator
    public SwarmInfo(Builder b) {
        this.clusterId = b.clusterId;
        this.manager = b.manager;
        this.nodeId = b.nodeId;
        this.managers = ImmutableList.copyOf(b.managers);
    }

    public static Builder builder() {
        return new Builder();
    }
}
