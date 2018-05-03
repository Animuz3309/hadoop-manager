package edu.scut.cs.hm.docker.cmd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DisconnectNetworkCmd {

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
     * Force the container to disconnect from the network.
     */
    @JsonProperty("Force")
    private boolean force;
}
