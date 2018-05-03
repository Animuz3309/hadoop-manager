package edu.scut.cs.hm.docker.cmd;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.scut.cs.hm.docker.model.network.Network;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * https://github.com/moby/moby/blob/master/api/types/types.go#L418
 */
@Data
public class CreateNetworkCmd {

    @JsonProperty("Name")
    private String name;

    @JsonProperty("CheckDuplicate")
    private boolean checkDuplicate;

    @JsonProperty("EnableIPv6")
    private boolean enableIpv6;

    @JsonProperty("Driver")
    private String driver;

    @JsonProperty("Internal")
    private boolean internal;

    @JsonProperty("Attachable")
    private boolean attachable;

    @JsonProperty("IPAM")
    private Network.Ipam ipam;

    @JsonProperty("Options")
    private final Map<String, String> options = new HashMap<>();

    @JsonProperty("Labels")
    private final Map<String, String> labels = new HashMap<>();
}
