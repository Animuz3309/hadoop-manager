package edu.scut.cs.hm.model.source;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import edu.scut.cs.hm.common.utils.Cloneables;
import edu.scut.cs.hm.docker.model.network.Port;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonPropertyOrder({"name", "image", "cluster", "application", "labels", "ports"})
@Data
@ToString(callSuper = true)
public class ServiceSource implements Cloneable, Comparable<ServiceSource> {
    private String id;
    private String name;
    /**
     * Name of swarm in which container will be created
     */
    private String cluster;
    /**
     * Application name
     */
    private String application;
    @Setter(AccessLevel.NONE)
    @JsonPropertyOrder(alphabetic = true)
    private List<Port> ports = new ArrayList<>();
    @Setter(AccessLevel.NONE)
    @JsonPropertyOrder(alphabetic = true)
    private Map<String, String> labels = new HashMap<>();
    @Setter(AccessLevel.NONE)
    @JsonPropertyOrder(alphabetic = true)
    private List<String> constraints = new ArrayList<>();
    @Setter(AccessLevel.NONE)
    private ContainerSource container = new ContainerSource();
    private Mode mode;

    /**
     * Set clone of argument as container.
     * @param container  argument for cloning, can be null
     */
    public void setContainer(ContainerSource container) {
        this.container = Cloneables.clone(container);
    }

    @Override
    public ServiceSource clone() {
        ServiceSource clone;
        try {
            clone = (ServiceSource) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        clone.ports = Cloneables.clone(clone.ports);
        clone.labels = Cloneables.clone(clone.labels);
        clone.constraints = Cloneables.clone(clone.constraints);
        clone.container = Cloneables.clone(clone.container);
        return clone;
    }

    @Override
    public int compareTo(ServiceSource o) {
        return ObjectUtils.compare(this.name, o.name);
    }

    @JsonSubTypes({
            @JsonSubTypes.Type(value = GlobalMode.class, name = "global"),
            @JsonSubTypes.Type(value = ReplicatedMode.class, name = "replicated")
    })
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, visible = true, property = "type")
    public interface Mode {
    }

    public static class GlobalMode implements Mode {

    }

    @Data
    public static class ReplicatedMode implements Mode {
        private long replicas;
    }
}