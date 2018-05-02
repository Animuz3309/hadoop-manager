package edu.scut.cs.hm.docker.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import edu.scut.cs.hm.model.node.NodeInfo;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.*;

/**
 * Information about Docker service and its nodes.
 */
@Data
public class DockerServiceInfo {
    @Data
    public static final class Builder {
        private String id;
        private String name;
        private ZonedDateTime systemTime;
        private Integer images;
        private Integer ncpu;
        private long memory;
        private Integer nodeCount;
        private Integer offNodeCount;
        private final List<NodeInfo> nodeList = new ArrayList<>();
        private final Map<String, String> labels = new HashMap<>();

        private Builder() {
        }

        public DockerServiceInfo build() {
            return new DockerServiceInfo(this);
        }

        public Builder from(DockerServiceInfo o) {
            setId(o.getId());
            setName(o.getName());
            setSystemTime(o.getSystemTime());
            setImages(o.getImages());
            setNcpu(o.getNcpu());
            setMemory(o.getMemory());
            setNodeList(o.getNodeList());
            setNodeCount(o.getNodeCount());
            setOffNodeCount(o.getOffNodeCount());
            setLabels(o.getLabels());
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder systemTime(ZonedDateTime systemTime) {
            setSystemTime(systemTime);
            return this;
        }

        public Builder images(Integer images) {
            this.images = images;
            return this;
        }

        public Builder ncpu(Integer ncpu) {
            this.ncpu = ncpu;
            return this;
        }

        public Builder memory(long memory) {
            this.memory = memory;
            return this;
        }

        public Builder nodeCount(Integer nodeCount) {
            this.nodeCount = nodeCount;
            return this;
        }

        public Builder offNodeCount(Integer offNodeCount) {
            setOffNodeCount(offNodeCount);
            return this;
        }

        public Builder nodeList(List<? extends NodeInfo> nodeList) {
            setNodeList(nodeList);
            return this;
        }

        public void setNodeList(Collection<? extends NodeInfo> nodeList) {
            this.nodeList.clear();
            if(nodeList != null) {
                this.nodeList.addAll(nodeList);
            }
        }

        public void setLabels(Map<String, String> labels) {
            this.labels.clear();
            if(labels != null) {
                this.labels.putAll(labels);
            }
        }
    }

    private final String id;
    private final String name;
    private final ZonedDateTime systemTime;
    private final Integer images;
    private final Integer ncpu;
    private final long memory;
    private final Integer nodeCount;
    private final Integer offNodeCount;
    private final List<NodeInfo> nodeList;
    private final Map<String, String> labels;

    public static Builder builder() {
        return new Builder();
    }

    private DockerServiceInfo(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.systemTime = builder.systemTime;
        this.images = builder.images;
        this.ncpu = builder.ncpu;
        this.memory = builder.memory;
        this.nodeCount = builder.nodeCount;
        this.offNodeCount = builder.offNodeCount;
        this.nodeList = ImmutableList.copyOf(builder.nodeList);
        this.labels = ImmutableMap.copyOf(builder.labels);
    }
}
