package edu.scut.cs.hm.model.node;

/**
 * Provider for {@link NodeInfo}
 */
public interface NodeInfoProvider {

    /**
     * Get {@link NodeInfo} from name of node
     * @param node name of node
     * @return NodeInfo
     */
    NodeInfo getNodeInfo(String node);

    /**
     * Get node's cluster
     * @param node
     * @return
     */
    default String getNodeCluster(String node) {
        NodeInfo nodeInfo = getNodeInfo(node);
        return nodeInfo == null ? null : nodeInfo.getCluster();
    }
}
