package edu.scut.cs.hm.docker.model.health;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * HealthConfig holds configuration settings for the HEALTHCHECK feature. <p/>
 * https://github.com/moby/moby/blob/master/api/types/container/config.go#L11
 */
@Data
public class HealthConfig {

    /**
     * disable healthcheck
     * @see #getTest()
     */
    public static final String TEST_NONE = "NONE";
    /**
     * {"CMD", args...} : exec arguments directly
     * @see #getTest()
     */
    public static final String TEST_CMD = "CMD";
    /**
     * {"CMD-SHELL", command} : run command with system's default shell
     * @see #getTest()
     */
    public static final String TEST_SHELL = "CMD-SHELL";

    /**
     * Test is the test to perform to check that the container is healthy.
     * An empty slice means to inherit the default.
     * The options are:
     * {} : inherit healthcheck
     * {"NONE"} : disable healthcheck
     * {"CMD", args...} : exec arguments directly
     * {"CMD-SHELL", command} : run command with system's default shell
     * @see #TEST_NONE
     * @see #TEST_CMD
     * @see #TEST_SHELL
     */
    @JsonProperty("Test")
    private final List<String> test;

    /**
     * Interval is the time to wait between checks. Zero means to inherit. Durations are expressed as nanoseconds.
     */
    @JsonProperty("Interval")
    private final long interval;

    /**
     * Timeout is the time to wait before considering the check to have hung. Zero means to inherit. Durations are expressed as nanoseconds.
     */
    @JsonProperty("Timeout")
    private final long timeout;

    /**
     * Retries is the number of consecutive failures needed to consider a container as unhealthy.
     * Zero means inherit.
     */
    @JsonProperty("Retries")
    private final int retires;
}