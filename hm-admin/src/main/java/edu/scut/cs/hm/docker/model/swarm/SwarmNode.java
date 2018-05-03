package edu.scut.cs.hm.docker.model.swarm;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.scut.cs.hm.docker.model.discovery.Node;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Node DTO for 'docker in swarm mode' which returned from '/nodes'. <p/>
 * Do not confuse with {@link Node } <p/>
 * <pre>
 * {
 *  "ID": "24ifsmvkjbyhk",
 *  "Version": {"Index": 8},
 *  "CreatedAt": "2016-06-07T20:31:11.853781916Z",
 *  "UpdatedAt": "2016-06-07T20:31:11.999868824Z",
 *  "Spec": {
 *      "Name": "my-node",
 *      "Role": "manager",
 *      "Availability": "active"
 *      "Labels": {"foo": "bar"}
 *  },
 *  "Description": {
 *      "Hostname": "bf3067039e47",
 *      "Platform": { "Architecture": "x86_64", "OS": "linux" },
 *      "Resources": { "NanoCPUs": 4000000000, "MemoryBytes": 8272408576 },
 *      "Engine": {
 *          "EngineVersion": "1.12.0-dev",
 *          "Labels": { "foo": "bar" },
 *          "Plugins": [ { "Type": "Volume", "Name": "local"} ]
 *      }
 *  },
 *  "Status": { "State": "ready" },
 *  "ManagerStatus": {
 *      "Leader": true,
 *      "Reachability": "reachable",
 *      "Addr": "172.17.0.2:2377""
 *  }
 * }
 * </pre>
 */
@Data
public class SwarmNode {

    @JsonProperty("ID")
    private final String id;

    @JsonProperty("Version")
    private final SwarmVersion version;

    @JsonProperty("CreatedAt")
    private final LocalDateTime created;

    @JsonProperty("UpdatedAt")
    private final LocalDateTime updated;

    @JsonProperty("Spec")
    private final Spec spec;

    @JsonProperty("Description")
    private final Description description;

    @JsonProperty("Status")
    private final State status;

    @JsonProperty("ManagerStatus")
    private final ManagerStatus managerStatus;

    @Data
    public static class Spec {

        @JsonProperty("Name")
        private final String name;

        @JsonProperty("Role")
        private final NodeRole role;

        @JsonProperty("Availability")
        private final NodeAvailability availability;

        @JsonProperty("Labels")
        private final Map<String, String> labels;
    }

    @Data
    public static class Description {

        @JsonProperty("Hostname")
        private final String hostname;

        @JsonProperty("Platform")
        private final Platform platform;

        @JsonProperty("Resources")
        private final TaskResources resources;

        @JsonProperty("Engine")
        private final Engine engine;
    }

    @Data
    public static class Engine {

        @JsonProperty("EngineVersion")
        private final String version;

        @JsonProperty("Labels")
        private final Map<String, String> labels;

        @JsonProperty("Plugins")
        private final List<Plugin> plugins;
    }

    @Data
    public static class Plugin {

        @JsonProperty("Type")
        private final String type;

        @JsonProperty("Name")
        private final String name;
    }

    @Data
    public static class State {

        @JsonProperty("State")
        private final NodeState state;

        @JsonProperty("Addr")
        private final String address;
    }

    @Data
    public static class ManagerStatus {
        @JsonProperty("Leader")
        private final boolean leader;

        @JsonProperty("Reachability")
        private final Reachability reachability;

        @JsonProperty("Addr")
        private final String address;
    }

    @Data
    public static class Platform {

        @JsonProperty("Architecture")
        private final String arch;

        @JsonProperty("OS")
        private final String os;
    }

    /**
     *
     * see https://github.com/moby/moby/blob/master/api/types/swarm/node.go#L103
     */
    public enum NodeState {
        UNKNOWN,
        DOWN,
        READY,
        DISCONNECTED
    }

    /**
     * https://github.com/moby/moby/blob/master/api/types/swarm/node.go#L84
     */
    public enum Reachability {
        UNKNOWN,
        UNREACHABLE,
        REACHABLE
    }

    /**
     * https://github.com/moby/moby/blob/master/api/types/swarm/node.go#L38
     */
    public enum NodeAvailability {
        ACTIVE,
        PAUSE,
        DRAIN
    }

    /**
     * https://github.com/moby/moby/blob/master/api/types/swarm/node.go#L28
     */
    public enum NodeRole {
        WORKER,
        MANAGER
    }
}
