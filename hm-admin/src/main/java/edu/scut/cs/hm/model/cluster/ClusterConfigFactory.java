package edu.scut.cs.hm.model.cluster;

import edu.scut.cs.hm.model.ngroup.AbstractNodesGroupConfig;

/**
 * Create Nodes group config from {@link ClusterCreationContext}
 */
public interface ClusterConfigFactory {

    /**
     * Create Nodes group config from {@link ClusterCreationContext}
     * @param ccc
     * @return
     */
    AbstractNodesGroupConfig<?> create(ClusterCreationContext ccc);
}
