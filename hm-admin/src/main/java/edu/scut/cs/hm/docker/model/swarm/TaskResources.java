package edu.scut.cs.hm.docker.model.swarm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * https://github.com/moby/moby/blob/master/api/types/swarm/task.go#L70
 */
@Data
@AllArgsConstructor
@Builder(builderClassName = "Builder")
public class TaskResources {

    /**
     * CPU limit in units of 10^-9 CPU shares.
     */
    @JsonProperty("NanoCPUs")
    private final long nanoCPUs;

    @JsonProperty("MemoryBytes")
    private final long memory;
}
