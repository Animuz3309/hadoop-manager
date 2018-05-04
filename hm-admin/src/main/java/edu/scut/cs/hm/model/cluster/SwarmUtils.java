package edu.scut.cs.hm.model.cluster;

import edu.scut.cs.hm.admin.config.configurer.DockerConfigurer;

public final class SwarmUtils {
    public static final String LABEL_SERVICE_ID = "com.docker.swarm.service.id";
    private static final String PROP_NODES_UPDATE = "hm.docker.node.updateSeconds";
    /**
     * @see DockerConfigurer#getNode()
     * ${hm.docker.node.updateSeconds}
     */
    public static final String EXPR_NODES_UPDATE = "${" + SwarmUtils.PROP_NODES_UPDATE + "}";
    private SwarmUtils() {}
}
