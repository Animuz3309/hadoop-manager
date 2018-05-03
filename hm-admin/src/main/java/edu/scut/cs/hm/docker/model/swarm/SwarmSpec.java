package edu.scut.cs.hm.docker.model.swarm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * <pre>
 "Spec": {
 "Orchestration": {},
 "Raft": {},
 "Dispatcher": {},
 "CAConfig": {}
 }
 * </pre>
 Also can be used for update command
 */
@Data
public class SwarmSpec {

    /**
     * Now undocumented, and we not known what it mean. Usual value "default".
     */
    @JsonProperty("Name")
    private String name;

    @JsonProperty("Orchestration")
    private OrchestrationConfig orchestration;

    @JsonProperty("Raft")
    private RaftConfig raft;

    @JsonProperty("Dispatcher")
    private DispatcherConfig dispatcher;

    @JsonProperty("CAConfig")
    private CaConfig caConfig;

    @JsonProperty("TaskDefaults")
    private TaskDefaults taskDefaults;

}
