package edu.scut.cs.hm.docker.model.network;

import edu.scut.cs.hm.docker.model.swarm.Endpoint;
import edu.scut.cs.hm.model.ProtocolType;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Docker container port
 */
@Data
@AllArgsConstructor
public class Port {
    private final int privatePort;
    private final int publicPort;
    private final ProtocolType type;
    private final Endpoint.PortConfigPublishMode mode;

    public Port(int privatePort, int publicPort, ProtocolType type) {
        this.privatePort = privatePort;
        this.publicPort = publicPort;
        this.type = type;
        this.mode = Endpoint.PortConfigPublishMode.HOST;
    }
}
