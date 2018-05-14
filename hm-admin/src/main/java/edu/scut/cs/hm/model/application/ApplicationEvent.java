package edu.scut.cs.hm.model.application;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableList;
import edu.scut.cs.hm.model.LogEvent;
import edu.scut.cs.hm.model.WithCluster;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApplicationEvent extends LogEvent implements WithCluster {
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Builder extends LogEvent.Builder<Builder, ApplicationEvent> {

        private String applicationName;
        private String fileName;
        private String clusterName;
        private final List<String> containers = new ArrayList<>();

        public Builder applicationName(String applicationName) {
            setApplicationName(applicationName);
            return this;
        }

        public Builder fileName(String fileName) {
            setFileName(fileName);
            return this;
        }

        public Builder clusterName(String clusterName) {
            setClusterName(clusterName);
            return this;
        }

        public Builder containers(List<String> containers) {
            setContainers(containers);
            return this;
        }

        public void setContainers(List<String> containers) {
            this.containers.clear();
            if(containers != null) {
                this.containers.addAll(containers);
            }
        }

        @Override
        public ApplicationEvent build() {
            return new ApplicationEvent(this);
        }
    }
    public static final String BUS = "bus.hm.log.application";

    private final String applicationName;
    private final String fileName;
    private final String clusterName;
    private final List<String> containers;

    @JsonCreator
    public ApplicationEvent(Builder b) {
        super(b);
        this.applicationName = b.applicationName;
        this.fileName = b.fileName;
        this.clusterName = b.clusterName;
        this.containers = ImmutableList.copyOf(b.containers);
    }

    @Override
    public String getCluster() {
        return this.clusterName;
    }

    public static Builder builder() {
        return new Builder();
    }
}
