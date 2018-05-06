package edu.scut.cs.hm.docker.model.container;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableMap;
import edu.scut.cs.hm.model.ContainerBaseIface;
import edu.scut.cs.hm.model.WithNode;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Base for docker container
 */
@Data
public final class ContainerBase implements ContainerBaseIface, WithNode {

    @Data
    public static class Builder implements ContainerBaseIface, WithNode {
        protected String id;
        protected String name;
        protected String image;
        protected String imageId;
        protected String node;
        protected DockerContainer.State state;
        protected final Map<String, String> labels = new HashMap<>();

        public void setLabels(Map<String, String> labels) {
            this.labels.clear();
            if(labels != null) {
                this.labels.putAll(labels);
            }
        }

        @SuppressWarnings("unchecked")
        public ContainerBase build() {
            return new ContainerBase(this);
        }

        public Builder from(ContainerBaseIface c) {
            setId(c.getId());
            setName(c.getName());
            setImage(c.getImage());
            setImageId(c.getImageId());
            if(c instanceof WithNode) {
                setNode(((WithNode)c).getNode());
            }
            if(c instanceof ContainerBase) {
                setState(((ContainerBase)c).getState());
            }
            setLabels(c.getLabels());
            return this;
        }
    }

    public static Builder builder() {
        return new Builder();
    }


    public static ContainerBase of(ContainerBaseIface c) {
        ContainerBase dc;
        if(c instanceof ContainerBase) {
            dc = (ContainerBase) c;
        } else {
            ContainerBase.Builder b = ContainerBase.builder();
            b.from(c);
            dc = b.build();
        }
        return dc;
    }


    private final String id;
    private final String name;
    private final String image;
    private final String imageId;
    private final String node;
    private final DockerContainer.State state;
    private final Map<String, String> labels;

    @JsonCreator
    public ContainerBase(Builder b) {
        this.id = b.id;
        this.name = b.name;
        this.image = b.image;
        this.imageId = b.imageId;
        this.node = b.node;
        this.state = b.state;
        this.labels = ImmutableMap.copyOf(b.labels);
    }

}
