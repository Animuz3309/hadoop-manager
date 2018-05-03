package edu.scut.cs.hm.docker.cmd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * Command for update node
 * @see edu.scut.cs.hm.docker.arg.NodeUpdateArg
 */
@Data
public class UpdateNodeCmd {
    /**
     * Node id from swarm mode of docker. <p/>
     * Not confuse it with node name or address, it must be string like '24ifsmvkjbyhk'.
     */
    @JsonIgnore
    private String nodeId;

    @JsonIgnore
    private long version;

    /**
     * Name for the node.
     */
    @JsonProperty("Name")
    private String name;

    /**
     * User-defined key/value metadata.
     */
    @JsonProperty("Labels")
    private Map<String, String> labels;

    /**
     * Role of the node.
     */
    @JsonProperty("Role")
    private Role role;

    /**
     * Availability of the node.
     */
    @JsonProperty("Availability")
    private Availability availability;

    public enum Role {
        WORKER, MANAGER
    }

    public enum Availability {
        ACTIVE, PAUSE, DRAIN
    }
}
