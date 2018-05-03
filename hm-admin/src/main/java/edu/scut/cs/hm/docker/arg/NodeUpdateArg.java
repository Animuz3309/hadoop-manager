package edu.scut.cs.hm.docker.arg;

import edu.scut.cs.hm.docker.cmd.UpdateNodeCmd;
import edu.scut.cs.hm.model.node.NodeInfo;
import lombok.Data;

import java.util.Map;

/**
 * Update node, but sometimes the node may united by 'swarm' or 'swarm mode'
 * so we add some other information
 * @see NodeInfo
 */
@Data
public class NodeUpdateArg {
    /**
     * Code from 'node.version'
     * @see NodeInfo#getVersion()
     */
    private long version;
    private String node;

    private Map<String, String> labels;

    /**
     * Role of the node.
     */
    private UpdateNodeCmd.Role role;

    /**
     * Availability of the node.
     */
    private UpdateNodeCmd.Availability availability;
}
