package edu.scut.cs.hm.docker.model.container;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.scut.cs.hm.model.ProtocolType;

/**
 * Docker container port
 */
public class Port {
    private static final String TYPE = "Type";
    private static final String PUBLIC_PORT = "PublicPort";
    private static final String PRIVATE_PORT = "PrivatePort";
    private static final String IP = "IP";

    private final String ip;
    private final int privatePort;
    private final int publicPort;
    private final ProtocolType type;

    public Port(@JsonProperty(IP) String ip,
                @JsonProperty(PRIVATE_PORT) int privatePort,
                @JsonProperty(PUBLIC_PORT) int publicPort,
                @JsonProperty(TYPE) ProtocolType type) {
        this.ip = ip;
        this.privatePort = privatePort;
        this.publicPort = publicPort;
        this.type = type;
    }

    @JsonProperty(IP)
    public String getIp() {
        return ip;
    }

    @JsonProperty(PRIVATE_PORT)
    public int getPrivatePort() {
        return privatePort;
    }

    @JsonProperty(PUBLIC_PORT)
    public int getPublicPort() {
        return publicPort;
    }

    @JsonProperty(TYPE)
    public ProtocolType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Port{" +
                "ip='" + ip + '\'' +
                ", privatePort=" + privatePort +
                ", publicPort=" + publicPort +
                ", type=" + type +
                '}';
    }
}
