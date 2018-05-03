package edu.scut.cs.hm.docker.res;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SwarmInitResult extends ServiceCallResult {
    /**
     * Swarm-mode Node id, do not confuse with docker node id, and node name. It value changed ant each join to
     * swarm or swarm init operation.
     */
    private String nodeId;
}
