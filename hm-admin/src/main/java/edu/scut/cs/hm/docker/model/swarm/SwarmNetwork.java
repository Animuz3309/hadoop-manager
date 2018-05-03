package edu.scut.cs.hm.docker.model.swarm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * https://github.com/moby/moby/blob/master/api/types/swarm/network.go
 */
@Data
public class SwarmNetwork {

    @JsonProperty("ID")
    private final String id;

    @JsonProperty("Version")
    private final SwarmVersion version;

    @JsonProperty("CreatedAt")
    private final LocalDateTime created;

    @JsonProperty("UpdatedAt")
    private final LocalDateTime updated;

    @JsonProperty("Spec")
    private final NetworkSpec spec;

    @JsonProperty("DriverState")
    private final Driver driverState;

    @JsonProperty("IPAMOptions")
    private final IPAMOptions IPAMOptions;

    @Data
    public static class NetworkSpec {

        @JsonProperty("Name")
        private final String name;

        @JsonProperty("Labels")
        private final Map<String, String> labels;

        @JsonProperty("DriverConfiguration")
        private final Driver driverConfiguration;

        @JsonProperty("IPv6Enabled")
        private final boolean IPv6Enabled;

        @JsonProperty("Internal")
        private final boolean internal;

        @JsonProperty("Attachable")
        private final boolean Attachable;

        @JsonProperty("IPAMOptions")
        private final IPAMOptions IPAMOptions;
    }

    @Data
    public static class NetworkAttachmentConfig {

        @JsonProperty("Target")
        private final String target;

        @JsonProperty("Aliases")
        private final List<String> aliases;
    }

    @Data
    public static class NetworkAttachment {

        @JsonProperty("Network")
        private final SwarmNetwork network;

        @JsonProperty("Addresses")
        private final List<String> addresses;
    }

    @Data
    public static class IPAMOptions {

        @JsonProperty("Driver")
        private final Driver driver;

        @JsonProperty("Configs")
        private final List<IPAMConfig> configs;
    }

    @Data
    public static class IPAMConfig {

        @JsonProperty("Subnet")
        private final String subnet;

        @JsonProperty("Range")
        private final String range;

        @JsonProperty("Gateway")
        private final String gateway;
    }
}
