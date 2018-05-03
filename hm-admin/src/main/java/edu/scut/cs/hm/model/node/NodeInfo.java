package edu.scut.cs.hm.model.node;

import edu.scut.cs.hm.model.Labels;
import edu.scut.cs.hm.model.WithCluster;

/**
 * Node information
 */
public interface NodeInfo extends Node, Labels, WithCluster {

    /**
     * version of node record, user on update requests
     */
    long getVersion();

    /**
     * Note that this id generated at join node to cluster, and can be changed any time,
     * therefore you can not identity node by this.
     * @return id or null
     */
    String getIdInCluster();

    /**
     * Flag which show that node now is power up and online. It not
     * meant that node is health or not.
     */
    boolean isOn();

    /**
     * Real cluster who own this node
     * @return name of real cluster or null.
     */
    @Override
    String getCluster();

    /**
     * May be null, or it may be outdated
     * @return
     */
    NodeMetrics getHealth();
}
