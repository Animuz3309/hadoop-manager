package edu.scut.cs.hm.docker.model.network;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * https://github.com/moby/moby/blob/51a9119f6b817bbae21805ec05787d462c9492cd/api/types/network/network.go#L46
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@lombok.Builder(builderClassName = "Builder")
@AllArgsConstructor
public class EndpointSettings {

    @JsonProperty("IPAMConfig")
    private final EndpointIPAMConfig ipamConfig;

    @JsonProperty("NetworkID")
    private final String networkID;

    @JsonProperty("EndpointID")
    private final String endpointId;

    @JsonProperty("Gateway")
    private final String gateway;

    @JsonProperty("IPAddress")
    private final String ipAddress;

    @JsonProperty("IPPrefixLen")
    private final Integer ipPrefixLen;

    @JsonProperty("IPv6Gateway")
    private final String ipV6Gateway;

    @JsonProperty("GlobalIPv6Address")
    private final String globalIPv6Address;

    @JsonProperty("GlobalIPv6PrefixLen")
    private final Integer globalIPv6PrefixLen;

    @JsonProperty("MacAddress")
    private final String macAddress;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @lombok.Builder(builderClassName = "Builder")
    @AllArgsConstructor
    public static class EndpointIPAMConfig {
        @JsonProperty("IPv4Address")
        private final String ipv4Address;

        @JsonProperty("IPv6Address")
        private final String ipv6Address;

        @JsonProperty("LinkLocalIPs")
        private final List<String> linkLocalIPs;
    }
}
