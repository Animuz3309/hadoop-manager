package edu.scut.cs.hm.docker.model.container;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import edu.scut.cs.hm.docker.model.network.Port;
import edu.scut.cs.hm.model.WithNode;
import lombok.Data;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple copy of docker container with additional field
 */
@Data
public class DockerContainer implements ContainerBaseIface, WithNode {

    /**
     * https://github.com/moby/moby/blob/51a9119f6b817bbae21805ec05787d462c9492cd/container/state.go
     * <code>type State struct</code>
     */
    public enum State {
        PAUSED(true),
        RESTARTING(true),
        RUNNING(true),
        REMOVING(false),
        DEAD(false),
        CREATED(false),
        EXITED(false);

        private final boolean run;

        State(boolean run) {
            this.run = run;
        }

        public boolean isRun() {
            return run;
        }

        public static State fromString(String str) {
            if(str == null) {
                return null;
            }
            try {
                return State.valueOf(str.toUpperCase());
            } catch (Exception e) {
                return null;
            }
        }
    }

    @Data
    public static class Builder implements ContainerBaseIface, WithNode {
        private String id;
        private String name;
        private String image;
        private String imageId;
        private String command;
        private long created;
        private final List<Port> ports = new ArrayList<>();
        private final Map<String, String> labels = new HashMap<>();
        private String status;
        private State state;
        private String node;

        public Builder id(String id) {
            setId(id);
            return this;
        }

        public Builder name(String name) {
            setName(name);
            return this;
        }

        public Builder image(String image) {
            setImage(image);
            return this;
        }

        public Builder imageId(String imageId) {
            setImageId(imageId);
            return this;
        }

        public Builder command(String command) {
            setCommand(command);
            return this;
        }

        public Builder created(long created) {
            setCreated(created);
            return this;
        }

        public Builder ports(List<Port> ports) {
            setPorts(ports);
            return this;
        }

        public void setPorts(List<Port> ports) {
            this.ports.clear();
            if(ports != null) {
                this.ports.addAll(ports);
            }
        }

        public Builder labels(Map<String, String> labels) {
            setLabels(labels);
            return this;
        }

        public void setLabels(Map<String, String> labels) {
            this.labels.clear();
            if(labels != null) {
                this.labels.putAll(labels);
            }
        }

        public Builder status(String status) {
            setStatus(status);
            return this;
        }

        public Builder node(String node) {
            setNode(node);
            return this;
        }

        public Builder from(ContainerBaseIface container) {
            setId(container.getId());
            setName(container.getName());
            setImage(container.getImage());
            setImageId(container.getImageId());
            if(container instanceof DockerContainer) {
                DockerContainer dc = (DockerContainer) container;
                setCommand(dc.getCommand());
                setCreated(dc.getCreated());
                setPorts(dc.getPorts());
                setLabels(dc.getLabels());
                setStatus(dc.getStatus());
                setState(dc.getState());
                setNode(dc.getNode());
            }
            return this;
        }

        public DockerContainer build() {
            return new DockerContainer(this);
        }
    }

    private final String id;
    private final String name;
    private final String image;
    private final String imageId;
    private final String command;
    private final long created;
    private final List<Port> ports;
    private final Map<String, String> labels;
    private final String status;
    private final State state;
    /**
     * node name
     */
    private final String node;

    @JsonCreator
    public DockerContainer(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.image = builder.image;
        this.imageId = builder.imageId;
        this.command = builder.command;
        this.created = builder.created;
        this.ports = ImmutableList.copyOf(builder.ports);
        this.labels = ImmutableMap.copyOf(builder.labels);
        this.status = builder.status;
        this.state = builder.state;
        this.node = builder.node;
        // we must allow to se invalid containers too, therefore check only 'id'
        Assert.notNull(this.id, "id is null");
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        Builder b = new Builder();
        b.setId(this.id);
        b.setName(this.name);
        b.setNode(this.node);
        b.setImage(this.image);
        b.setImageId(this.imageId);
        b.setCommand(this.command);
        b.setCreated(this.created);
        b.setPorts(this.ports);
        b.setLabels(this.labels);
        b.setStatus(this.status);
        return b;
    }

    /**
     * value calculated from state
     * @see State#isRun()
     * @return true when container is run
     */
    public boolean isRun() {
        return state != null && state.isRun();
    }

    @Override
    public String toString() {
        return "DockerContainer{" +
                "id='" + id + '\'' +
                ", name=" + name +
                ", image='" + image + '\'' +
                ", command='" + command + '\'' +
                ", created=" + created +
                ", ports=" + ports +
                ", labels=" + labels +
                ", status='" + status + '\'' +
                ", state='" + state + '\'' +
                '}';
    }
}
