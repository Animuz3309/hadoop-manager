package edu.scut.cs.hm.model.node;

import lombok.Data;

@Data
public class NodeServiceConfig {
    /**
     * Maximal value of expected node count. <p/>
     * It not limit, it used for allocate some resources, so you can
     * exceed this value, but then performance may degrade.
     */
    private int maxNodes = 12;
    /**
     * Minimal ttl of node in seconds, note that node may not respond on maintenance
     * and we must keep they while this time.
     */
    private int minTtl = 60;
    /**
     * Time between node update
     */
    private int updateSeconds = 60;
}
