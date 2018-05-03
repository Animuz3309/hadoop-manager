package edu.scut.cs.hm.docker.model.swarm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DispatcherConfig {

    @JsonProperty("HeartbeatPeriod")
    private Long heartbeatPeriod;
}