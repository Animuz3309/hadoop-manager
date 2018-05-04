package edu.scut.cs.hm.model.cluster;

import edu.scut.cs.hm.model.ngroup.AbstractNodesGroupConfig;

public interface ClusterConfigFactory {
    AbstractNodesGroupConfig<?> create(ClusterCreationContext ccc);
}
