package edu.scut.cs.hm.docker.model.discovery;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

/**
 * Represent swarm node @see https://github.com/docker/swarm/blob/master/discovery/nodes/nodes.go
 * The node == host on which runs docker service.
 */
@AllArgsConstructor(onConstructor = @__(@JsonCreator))
@Data
public class Node {

    @JsonProperty("Addr")
    private final String addr;

    @JsonProperty("Cpus")
    private final int cpus;

    @JsonProperty("ID")
    private final String id;

    @JsonProperty("IP")
    private final String ip;

    @JsonProperty("Name")
    private final String name;

    @JsonProperty("Memory")
    private final long memory;

    @JsonProperty("Labels")
    private final Map<String, String> labels;
}
