package edu.scut.cs.hm.docker.model.swarm;

import edu.scut.cs.hm.model.cluster.AbstractNodesGroup;
import lombok.extern.slf4j.Slf4j;

/**
 * Nodes group network manager
 * //TODO not finished yet
 */
@Slf4j
public class NetworkManager {

    public static final String OVERLAY_DRIVER = "overlay";
    private final AbstractNodesGroup<?> group;

    public NetworkManager(AbstractNodesGroup<?> group) {
        this.group = group;
    }

    public void createNetwork(String networkName) {
        //todo finish create net work
    }
}
