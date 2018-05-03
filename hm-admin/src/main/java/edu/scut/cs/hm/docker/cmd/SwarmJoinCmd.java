package edu.scut.cs.hm.docker.cmd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 {
 "ListenAddr": "0.0.0.0:2377",
 "AdvertiseAddr": "192.168.1.1:2377",
 "RemoteAddrs": ["node1:2377"],
 "JoinToken": "SWMTKN-1-3pu6hszjas19xyp7ghgosyx9k8atbfcr8p2is99znpy26u2lkl-7p73s1dx5in4tatdymyhg9hu2"
 }
 */
@Data
public class SwarmJoinCmd {
    /**
     * 'Listen address used for inter-manager communication if the node gets promoted to manager, as well as
     * determining the networking interface used for the VXLAN Tunnel Endpoint (VTEP).'
     */
    @JsonProperty("ListenAddr")
    private String listen;

    /**
     * 'Externally reachable address advertised to other nodes.
     * If AdvertiseAddr is not specified, it will be automatically detected when possible.'
     */
    @JsonProperty("AdvertiseAddr")
    private String advertise;

    /**
     * 'Address of any manager node already participating in the swarm.'<p/>
     * Node that it not docker JSON API address!<p/>
     */
    @JsonProperty("RemoteAddrs")
    private final List<String> managers = new ArrayList<>();

    /**
     * Secret token for joining this swarm.
     */
    @JsonProperty("JoinToken")
    private String token;
}
