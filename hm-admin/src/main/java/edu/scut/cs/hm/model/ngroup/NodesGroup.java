package edu.scut.cs.hm.model.ngroup;

import edu.scut.cs.hm.model.container.ContainersManager;
import edu.scut.cs.hm.docker.DockerService;
import edu.scut.cs.hm.docker.arg.NodeUpdateArg;
import edu.scut.cs.hm.docker.res.ServiceCallResult;
import edu.scut.cs.hm.docker.model.swarm.NetworkManager;
import edu.scut.cs.hm.model.Named;
import edu.scut.cs.hm.model.WithAcl;
import edu.scut.cs.hm.model.node.NodeInfo;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Interface represent node group
 * node group means real ngroup like swarm or docker in swarm mode ngroup,
 * or combination of node satisfy {@link java.util.function.Predicate}
 */
public interface NodesGroup extends Named, WithAcl {
    enum Feature {
        /**
         * nodes in group is united by single 'swarm' service
         * less use now, so we don't support it at this moment
         */
        SWARM,

        /**
         * nodes in group is united by docker in 'swarm mode'
         */
        SWARM_MODE,

        /**
         * disallow node addition
         */
        FORBID_NODE_ADDITION,
        ;

    }

    /**
     * Try to init ngroup if it not init yet
     */
    void init();

    /**
     * Clean resources of node group (for example destroy ngroup)
     */
    void clean();

    /**
     * flush k-v volume
     */
    void flush();

    AbstractNodesGroupConfig<?> getConfig();
    void setConfig(AbstractNodesGroupConfig<?> config);
    void updateConfig(Consumer<AbstractNodesGroupConfig<?>> consumer);

    /**
     * State of ngroup.
     * @see #init()
     * @return state, can not be null.
     */
    NodeGroupState getState();

    /**
     * Identifier of ngroup
     * @return
     */
    String getName();

    /**
     * Human friendly ngroup name
     * @return
     */
    String getTitle();

    /**
     * SpEL string which applied to images. It evaluated over object with 'tag(name)' and 'label(key, val)' functions,
     * also it has 'r(regexp)' function which can combined with other,
     * like: <code>'spel:tag(r(".*_dev")) or label("dev", "true")'</code>.
     * @return
     */
    String getImageFilter();
    void setImageFilter(String imageFilter);

    /**
     * Cluster description
     * @return
     */
    String getDescription();
    void setDescrition(String descrition);

    /**
     *
     * @param id name of node, when we add a node use the name
     * @return
     */
    boolean hasNode(String id);

    /**
     * Return copy of all current nodes collection
     * @return copy of current nodes
     */
    List<NodeInfo> getNodes();

    /**
     * Update node but not only attributes of physical node, some 'node in docker swarm ngroup' attr
     * @param arg
     * @return
     */
    ServiceCallResult updateNode(NodeUpdateArg arg);

    /**
     * Collections with names of other intersected NodesGroups. Note that it
     * not mean 'enclosed' relationship. <p/>
     * The 'Real Cluster' means 'swarm ngroup' or 'docker in swarm mode ngroup'
     * Any RealCluster always return empty collection.
     * For example 'all' - return all real clusters
     * @return
     */
    Collection<String> getGroups();

    /**
     * When we use swarm or 'docker in swarm mode' we return manager node's docker service
     * @return
     */
    DockerService getDocker();

    /**
     * @see Feature
     * @return
     */
    Set<Feature> getFeatures();

    /**
     * Tool for managing ngroup containers, it replace for direct access to docker service
     * @return non null value
     */
    ContainersManager getContainers();

    /**
     * Tool for managing network between containers
     * @return
     */
    NetworkManager getNetworks();

    /**
     * Default network name
     * @return
     */
    String getDefaultNetworkName();
}
