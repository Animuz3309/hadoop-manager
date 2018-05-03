package edu.scut.cs.hm.docker.model.swarm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Base class for some DTOs.<p/>
 * See: https://github.com/moby/moby/blob/master/api/types/swarm/common.go#L11 <p/>
 * Note that it mutable version, we can not make immutable kind of this because lombok & jackson has some issues
 * which prevent it.
 */
@Data
public class MetaMutable {
    @JsonProperty("ID")
    private String id;
    @JsonProperty("Version")
    private SwarmVersion version;
    @JsonProperty("CreatedAt")
    private LocalDateTime created;
    @JsonProperty("UpdatedAt")
    private LocalDateTime updated;
}
