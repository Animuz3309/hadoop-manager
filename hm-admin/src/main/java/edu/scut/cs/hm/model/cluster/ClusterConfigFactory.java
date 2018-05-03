package edu.scut.cs.hm.model.cluster;

public interface ClusterConfigFactory {
    AbstractNodesGroupConfig<?> create(ClusterCreationContext ccc);
}
