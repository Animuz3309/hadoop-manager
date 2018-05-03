package edu.scut.cs.hm.docker.res;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.scut.cs.hm.docker.model.network.EndpointSettings;
import lombok.Data;

/**
 * https://github.com/moby/moby/blob/master/api/types/types.go#L442
 */
@Data
public class ConnectNetworkCmd {

    /**
     * Name or id of network
     */
    @JsonIgnore
    private String network;

    /**
     * name or id of container
     */
    @JsonProperty("Container")
    private String container;

    /**
     * Configuration for a network endpoint.
     */
    @JsonProperty("EndpointConfig")
    private EndpointSettings config;
}