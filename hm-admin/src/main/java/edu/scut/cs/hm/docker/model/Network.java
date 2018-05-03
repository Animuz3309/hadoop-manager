package edu.scut.cs.hm.docker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * Based on 'NetworkResource' from https://github.com/moby/moby/blob/master/api/types/types.go <p/>
 * Note that 'inspect' command return same, but more filled object that 'list'
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderClassName = "Builder")
@AllArgsConstructor
public class Network {

    @JsonProperty("Id")
    private final String id;

    @JsonProperty("Name")
    private final String name;

    @JsonProperty("Created")
    private final ZonedDateTime created;

    /**
     * Scope describes the level at which the network exists
     * (e.g. `global` for cluster-wide or `local` for machine level)
     */
    @JsonProperty("Scope")
    private final Scope scope;

    @JsonProperty("Driver")
    private final String driver;

    /**
     * IPAM is the network's IP Address Management
     */
    @JsonProperty("IPAM")
    private final Ipam ipam;

    /**
     * Internal represents if the network is used internal only
     */
    @JsonProperty("Internal")
    private final boolean internal;

    /**
     * Attachable represents if the global scope is manually attachable by regular containers
     * from workers in swarm mode.
     */
    @JsonProperty("Attachable")
    private final boolean attachable;

    @JsonProperty("EnableIPv6")
    private final boolean enableIpv6;

    @JsonProperty("Containers")
    private final Map<String, EndpointResource> containers;

    /**
     * Options holds the network specific options to use for when creating the network
     */
    @JsonProperty("Options")
    private final Map<String, String> options;

    @JsonProperty("Labels")
    private final Map<String, String> labels;

    /**
     * List of peer nodes for an overlay network
     */
    @JsonProperty("Peers")
    private final List<PeerInfo> peers;

    public enum Scope {
        GLOBAL, LOCAL, SWARM
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @lombok.Builder(builderClassName = "Builder")
    @AllArgsConstructor
    public static class EndpointResource {


        @JsonProperty("Name")
        private final String name;

        @JsonProperty("EndpointID")
        private final String endpointId;

        @JsonProperty("MacAddress")
        private final String macAddress;

        @JsonProperty("IPv4Address")
        private final String ipv4Address;

        @JsonProperty("IPv6Address")
        private final String ipv6Address;

    }

    @Data
    @lombok.Builder(builderClassName = "Builder")
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Ipam {

        public static Ipam DEF_IPAM = Ipam.builder().driver("default").build();

        @JsonProperty("Driver")
        private final String driver;

        /**
         * Per network IPAM driver options
         */
        @JsonProperty("Options")
        private final Map<String, String> options;

        @Singular
        @JsonProperty("Config")
        private final List<IpamConfig> configs;

    }

    @Data
    @lombok.Builder(builderClassName = "Builder")
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IpamConfig {

        @JsonProperty("Subnet")
        private final String subnet;

        @JsonProperty("IPRange")
        private final String ipRange;

        @JsonProperty("Gateway")
        private final String gateway;

        @JsonProperty("AuxiliaryAddresses")
        private final Map<String, String> auxiliaryAddresses;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PeerInfo {

        @JsonProperty("Name")
        private final String name;

        @JsonProperty("IP")
        private final String ip;

    }

}
