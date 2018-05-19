package edu.scut.cs.hm.docker.model.volume;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.scut.cs.hm.common.json.JtEnumLower;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

/**
 * https://github.com/moby/moby/blob/30d0c3899ef220e2a40f70e1e888a2d41fd7e7e8/api/types/volume.go
 */
@Data
@AllArgsConstructor
@lombok.Builder(builderClassName = "Builder")
public class Volume {

    /**
     * Name of the volume driver used by the volume.
     * Required: true
     */
    @JsonProperty("Driver")
    private final String driver;

    /**
     * User-defined key/value metadata.
     * Required: true
     */
    @JsonProperty("Labels")
    private final Map<String, String> labels;

    /**
     * Mount path of the volume on the host.
     * Required: true
     */
    @JsonProperty("Mountpoint")
    private final String mountpoint;

    /**
     * Name of the volume.
     * Required: true
     */
    @JsonProperty("Name")
    private final String name;

    /**
     * The driver specific options used when creating the volume.
     * Required: true
     */
    @JsonProperty("Options")
    private final Map<String, String> options;

    /**
     * The level at which the volume exists. Either `global` for ngroup-wide, or `local` for machine level.
     * Required: true
     */
    @JsonProperty("Scope")
    private final Scope scope;

    /**
     * Low-level details about the volume, provided by the volume driver.
     * Details are returned as a map with key/value pairs:
     * `{"key":"value","key2":"value2"}`.
     * <p>
     * The `Status` field is optional, and is omitted if the volume driver
     * does not support this feature.
     */
    @JsonProperty("Status")
    private final Map<String, String> status;

    /**
     * usage data
     */
    @JsonProperty("UsageData")
    private final VolumeUsageData usageData;


    @Data
    @AllArgsConstructor
    @lombok.Builder(builderClassName = "Builder")
    public static class VolumeUsageData {

        /**
         * The number of containers referencing this volume.
         */
        @JsonProperty("RefCount")
        private final int refCount;

        /**
         * The disk space used by the volume (local driver only)
         */
        @JsonProperty("Size")
        private final long size;
    }

    /**
     * The level at which the volume exists.
     */
    @JtEnumLower
    public enum Scope {
        /**
         * `global` for ngroup-wide
         */
        GLOBAL,
        /**
         * `local` for machine level
         */
        LOCAL
    }
}
