package edu.scut.cs.hm.docker.model.swarm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class JoinTokens {

    @JsonProperty("Worker")
    private String worker;

    @JsonProperty("Manager")
    private String manager;
}
