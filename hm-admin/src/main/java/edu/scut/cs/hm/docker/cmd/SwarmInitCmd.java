package edu.scut.cs.hm.docker.cmd;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.scut.cs.hm.docker.model.swarm.SwarmSpec;
import lombok.Data;

/**
 * {
 "ListenAddr": "0.0.0.0:2377",
 "AdvertiseAddr": "192.168.1.1:2377",
 "ForceNewCluster": false,
 "Spec": {
 "Orchestration": {},
 "Raft": {},
 "Dispatcher": {},
 "CAConfig": {}
 }
 }
 */
@Data
public class SwarmInitCmd {
    @JsonProperty("ListenAddr")
    private String listenAddr;
    @JsonProperty("AdvertiseAddr")
    private String advertiseAddr;
    @JsonProperty("ForceNewCluster")
    private String forceNewCluster;
    @JsonProperty("Spec")
    private SwarmSpec spec;
}
