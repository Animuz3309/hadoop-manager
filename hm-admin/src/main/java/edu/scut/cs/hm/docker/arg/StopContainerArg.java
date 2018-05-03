package edu.scut.cs.hm.docker.arg;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

/**
 * Argument for stop container
 */
@Data
@Builder
public class StopContainerArg {
    /**
     * Container ID
     */
    @JsonIgnore
    private final String id;

    private final int timeBeforeKill;

}
