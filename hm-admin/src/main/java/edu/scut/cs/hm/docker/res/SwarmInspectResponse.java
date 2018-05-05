package edu.scut.cs.hm.docker.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.scut.cs.hm.docker.model.swarm.Cluster;
import edu.scut.cs.hm.docker.model.swarm.JoinTokens;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Result of 'GET /swarm'
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SwarmInspectResponse extends Cluster {

    @JsonProperty("JoinTokens")
    private JoinTokens joinTokens;

}
