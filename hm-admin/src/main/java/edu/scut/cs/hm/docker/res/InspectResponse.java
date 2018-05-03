package edu.scut.cs.hm.docker.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.scut.cs.hm.docker.model.swarm.JoinTokens;
import edu.scut.cs.hm.docker.model.swarm.Cluster;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Result of 'GET /swarm'
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InspectResponse extends Cluster {

    @JsonProperty("JoinTokens")
    private JoinTokens joinTokens;

}
