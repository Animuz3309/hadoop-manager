package edu.scut.cs.hm.model.cluster;

import edu.scut.cs.hm.model.ExtendedAssert;
import edu.scut.cs.hm.model.ngroup.NodeGroupState;
import edu.scut.cs.hm.model.ngroup.NodesGroup;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 */
public final class ClusterUtils {

    private ClusterUtils() {
    }


    /**
     * Real ngroup must contain env name. Like 'envName:clusterName'.
     * @param name
     */
    public static void checkRealClusterName(String name) {
        checkEmpty(name);
    }

    private static void checkEmpty(String name) {
        if(!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Name is null or empty.");
        }
    }

    /**
     * Report 500 http error when ngroup in not OK state.
     * @param nodesGroup ngroup
     */
    public static void checkClusterState(NodesGroup nodesGroup) {
        NodeGroupState state = nodesGroup.getState();
        ExtendedAssert.error(state.isOk(), "Cluster '" + nodesGroup.getName() + "' is in no OK state: " + state.getMessage());
    }

    public static boolean isDockerBased(NodesGroup nodesGroup) {
        Set<NodesGroup.Feature> features = nodesGroup.getFeatures();
        return features.contains(NodesGroup.Feature.SWARM) ||
                features.contains(NodesGroup.Feature.SWARM_MODE);
    }
}

