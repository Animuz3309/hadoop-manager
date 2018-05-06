package edu.scut.cs.hm.model.cluster;

import edu.scut.cs.hm.docker.DockerService;
import edu.scut.cs.hm.model.ngroup.AbstractNodesGroupConfig;
import edu.scut.cs.hm.model.ngroup.NodesGroup;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Service of nodes group (include nodes group or real ngroup)
 * @see package-info.java
 */
public interface DiscoveryStorage {
    String GROUP_ID_ALL = "all";
    String GROUP_ID_ORPHANS = "orphans";

    Collection<String> SYSTEM_GROUPS = Arrays.asList(GROUP_ID_ALL, GROUP_ID_ORPHANS);

    /**
     * Return exists ngroup of concrete node.
     * @param node
     * @return ngroup or null
     */
    NodesGroup getClusterForNode(String node);

    /**
     * Return exists ngroup or null
     * @param clusterId
     * @return exists ngroup or null
     */
    NodesGroup getCluster(String clusterId);

    /**
     * Return existed ngroup or create new.
     * @param clusterId name of ngroup
     * @param factory factory or null
     * @return NodesGroup, never null
     */
    NodesGroup getOrCreateCluster(String clusterId, ClusterConfigFactory factory);

    /**
     * Register new group, or return already registered. Like {@link #getOrCreateCluster(String, ClusterConfigFactory)} but allow to
     * create node group and real clusters too.
     * @param config
     * @return registered node group.
     */
    NodesGroup getOrCreateCluster(AbstractNodesGroupConfig<?> config);

    /**
     * Delete ngroup by ngroup name (real ngroup)
     * @param clusterId ngroup name {@link NodesGroup#getName()}
     */
    void deleteCluster(String clusterId);

    /**
     * Delete ngroup by nodes group name (just in logic nodes group not real ngroup)
     * @param clusterId nodes group name {@link NodesGroup#getName()}
     */
    void deleteNodeGroup(String clusterId);

    /**
     * Get all ngroup (include nodes group and real ngroup)
     * @return
     */
    List<NodesGroup> getClusters();

    /**
     * Docker service
     * @param clusterId
     * @return
     */
    DockerService getService(String clusterId);

    /**
     * In docker swarm mode service means a combination of containers(maybe in different nodes)
     * @return
     */
    Set<String> getServices();
}
