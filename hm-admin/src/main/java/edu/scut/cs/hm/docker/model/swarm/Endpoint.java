package edu.scut.cs.hm.docker.model.swarm;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.scut.cs.hm.model.ProtocolType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

/**
 * https://github.com/moby/moby/blob/51a9119f6b817bbae21805ec05787d462c9492cd/api/types/swarm/network.go
 * TODO need finish
 */
@Data
public class Endpoint {
    @JsonProperty("Spec")
    private final EndpointSpec spec;

    @JsonProperty("Ports")
    private final List<PortConfig> ports;

    @JsonProperty("VirtualIPs")
    private final List<EndpointVirtualIP> virtualIPs;

    @Data
    @AllArgsConstructor
    @Builder(builderClassName = "Builder")
    public static class EndpointSpec {
        /**
         * The mode of resolution to use for internal load balancing between tasks (vip or dnsrr).
         * Defaults to vip if not provided.
         */
        @JsonProperty("Mode")
        private final ResolutionMode mode;

        /**
         * List of exposed ports that this service is accessible on from the outside.
         * Ports can only be provided if vip resolution mode is used.
         */
        @Singular
        @JsonProperty("Ports")
        private final List<PortConfig> ports;
    }

    /**
     * The mode of resolution to use for internal load balancing between tasks (vip or dnsrr).
     */
    public enum ResolutionMode {
        /**
         * VIP resolution mode specifies that the
         * service resolves to a logical IP and the requests
         * are sent to that logical IP. Packets hitting that
         * logical IP are load balanced to a chosen backend.
         */
        VIP,
        /**
         * DNS round-robin. <p/>
         * DNSRR resolution mode specifies that the
         * service directly gets resolved to one of the
         * backend IP and the client directly initiates a
         * request towards the actual backend. This requires
         * that the client does not cache the DNS responses
         * when the DNS response TTL is 0.
         */
        DNSRR
    }

    /**
     * PortConfig represents the config of a port.
     */
    @Data
    @AllArgsConstructor
    @Builder(builderClassName = "Builder")
    public static class PortConfig {

        @JsonProperty("Name")
        private final String name;

        @JsonProperty("Protocol")
        private final ProtocolType protocol;

        /**
         * TargetPort is the port inside the container
         */
        @JsonProperty("TargetPort")
        private final int targetPort;

        /**
         * PublishedPort is the port on the swarm hosts
         */
        @JsonProperty("PublishedPort")
        private final int publishedPort;

        /**
         * PublishMode is the mode in which port is published
         */
        @JsonProperty("PublishMode")
        private final PortConfigPublishMode publishMode;
    }

    public enum PortConfigPublishMode {
        /**
         * used for ports published
         * for ingress load balancing using routing mesh.
         */
        INGRESS,
        /**
         * used for ports published
         * for direct host level access on the host where the task is running.
         */
        HOST
    }

    /**
     * EndpointVirtualIP represents the virtual ip of a port.
     */
    @Data
    @AllArgsConstructor
    @Builder(builderClassName = "Builder")
    public static class EndpointVirtualIP {
        @JsonProperty("NetworkID")
        private final String networkId;
        @JsonProperty("Addr")
        private final String address;

    }
}