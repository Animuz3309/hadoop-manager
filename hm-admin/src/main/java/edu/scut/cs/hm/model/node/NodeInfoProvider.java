package edu.scut.cs.hm.model.node;

/**
 * Provider for {@link NodeInfo}
 */
public interface NodeInfoProvider {

    /**
     * Get {@link NodeInfo} from name of swarmNode
     * @param node name of swarmNode
     * @return NodeInfo
     */
    NodeInfo getNodeInf(String node);

    default String getNodeCluster(String node) {
        NodeInfo nodeInfo = getNodeInf(node);
        return nodeInfo == null ? null : nodeInfo.getCluster();
    }
}
