package edu.scut.cs.hm.model.cluster;

import edu.scut.cs.hm.model.ServiceCallResult;
import edu.scut.cs.hm.model.Named;
import edu.scut.cs.hm.model.WithAcl;
import edu.scut.cs.hm.model.node.NodeInfo;

import java.util.List;

/**
 * Interface represent node group
 */
public interface NodesGroup extends Named, WithAcl {
    enum Feature {
        /**
         * nodes in group is united by single 'swarm' service
         */
        SWARM,

        /**
         * nodes in group is united by docker in 'swarm mode'
         */
        SWARM_MOD,

        /**
         * disallow node addition
         */
        FORBID_NODE_ADDITION,
        ;
    }

    /**
     * flush k-v storage
     */
    void flush();

    /**
     * Try to init cluster if it not init yet
     */
    void init();

    /**
     * Clean resources of node group (for example destroy cluster)
     */
    void clean();

    /**
     * Identifier of cluster
     * @return
     */
    String getName();

    /**
     * Human friendly cluster name
     * @return
     */
    String getTitle();

    /**
     * Cluster description
     * @return
     */
    String getDescription();

    void setDescrition();

    /**
     * Return copy of all current nodes collection
     * @return copy of current nodes
     */
    List<NodeInfo> getNodes();

    ServiceCallResult updateNode()
}
