package edu.scut.cs.hm.docker.model.health;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * Representation of a Docker statistics.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Statistics {

    @JsonProperty("read")
    private String read;

    /**
     * @since Docker Remote API 1.21
     */
    @JsonProperty("networks")
    private Map<String, Object> networks;

    /**
     * @deprecated as of Docker Remote API 1.21, replaced by {@link #networks}
     */
    @Deprecated
    @JsonProperty("network")
    private Map<String, Object> network;

    @JsonProperty("memory_stats")
    private Map<String, Object> memoryStats;

    @JsonProperty("blkio_stats")
    private Map<String, Object> blkioStats;

    @JsonProperty("cpu_stats")
    private Map<String, Object> cpuStats;

    @JsonProperty("precpu_stats")
    private Map<String, Object> precpuStats;

}
