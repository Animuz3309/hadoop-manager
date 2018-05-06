package edu.scut.cs.hm.docker.model.container;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Network settings of docker image container
 */
public final class ContainerNetworkSettings {
    private static final String IP_ADDRESS = "IPAddress";
    //"Bridge":"docker0"
    // "Gateway":"172.17.42.1"
    // "GlobalIPv6Address":""
    // "GlobalIPv6PrefixLen":0
    // "IPAddress":"172.17.0.7"
    // "IPPrefixLen":16
    // "IPv6Gateway":""
    // "LinkLocalIPv6Address":"fe80::42:acff:fe11:7"
    // "LinkLocalIPv6PrefixLen":64
    // "MacAddress":"02:42:ac:11:00:07"
    // "PortMapping":null
    // "Ports":{"3306/tcp":[{"HostIp":"172.31.0.12","HostPort":"3306"}]}}

    private final String ipAddress;

    @JsonCreator
    public ContainerNetworkSettings(@JsonProperty(IP_ADDRESS) String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }
}
