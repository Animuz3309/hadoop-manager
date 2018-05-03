package edu.scut.cs.hm.model.cluster;

import edu.scut.cs.hm.admin.service.NodeService;
import edu.scut.cs.hm.docker.DockerService;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * Service of nodes group (include nodes group or real cluster)
 * @see package-info.java
 */
public interface ClusterService {
    String GROUP_ID_ALL = "all";
    String GROUP_ID_ORPHANS = "orphans";

    Collection<String> SYSTEM_GROUPS = Arrays.asList(GROUP_ID_ALL, GROUP_ID_ORPHANS);

    /**
     * Return exists cluster of concrete node.
     * @param node
     * @return cluster or null
     */
    NodesGroup getClusterForNode(String node);

    /**
     * Return exists cluster or null
     * @param clusterId
     * @return exists cluster or null
     */
    NodesGroup getCluster(String clusterId);

    /**
     * Return existed cluster or create new.
     * @param clusterId name of cluster
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
    NodesGroup getOrCreateGroup(AbstractNodesGroupConfig<?> config);

    /**
     * Delete cluster by cluster name (real cluster)
     * @param clusterId cluster name {@link NodesGroup#getName()}
     */
    void deleteCluster(String clusterId);

    /**
     * Delete cluster by nodes group name (just in logic nodes group not real cluster)
     * @param clusterId nodes group name {@link NodesGroup#getName()}
     */
    void deleteNodeGroup(String clusterId);

    /**
     * Get all cluster (include nodes group and real cluster)
     * @return
     */
    List<NodesGroup> getClusters();

    /**
     * Docker service
     * @param name
     * @return
     */
    DockerService getService(String name);

    /**
     * In docker swarm mode service means a combination of containers(maybe in different nodes)
     * @return
     */
    Set<String> getServices();


    /**
     * Get executor
     * TODO refactor
     * @return
     */
    ExecutorService getExecutor();

    /**
     * Get NodeService
     * TODO refactor
     * @return
     */
    NodeService getNodeService();
}
