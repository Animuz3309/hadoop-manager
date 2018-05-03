package edu.scut.cs.hm.docker.model.swarm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OrchestrationConfig {

    @JsonProperty("TaskHistoryRetentionLimit")
    private Integer taskHistoryRetentionLimit;

}
