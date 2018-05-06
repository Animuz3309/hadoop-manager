package edu.scut.cs.hm.model.cluster;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.scut.cs.hm.model.source.ContainerSource;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public final class SwarmUtils {
    public static final String LABELS_PREFIX = "com.docker.swarm.";
    public static final String LABEL_CONSTRAINTS = LABELS_PREFIX + "constraints";

    public static final String LABEL_SERVICE_ID = "com.docker.swarm.service.id";

    public static final String NODE_EQ = "node==";
    private SwarmUtils() {}

    /**
     * Remove all swarm data from labels
     * @param labels
     */
    public static void clearLabels(Map<String, String> labels) {
        // swarm save some info to container labels, we must not copy it constraints (because it
        // conflicts with our) and some other data
        labels.keySet().removeIf(k -> k.startsWith(LABELS_PREFIX));
    }

    public static void clearConstraints(Map<String, String> labels) {
        labels.remove(LABEL_CONSTRAINTS);
    }

    public static void restoreEnv(ObjectMapper objectMapper, ContainerSource containerSource) {
        String constraintStr = containerSource.getLabels().get(LABEL_CONSTRAINTS);
        if(constraintStr == null) {
            return;
        }
        try {
            String[] constraints = objectMapper.readValue(constraintStr, String[].class);
            List<String> env = containerSource.getEnvironment();
            final String node = containerSource.getNode();
            for(String constraint: constraints) {
                if(node != null &&
                        constraint.startsWith(NODE_EQ) &&
                        constraint.regionMatches(NODE_EQ.length(), node, 0, node.length())) {
                    //the node eq constraint is granted by node name, we must skip this restoring
                    continue;
                }
                env.add("constraint:" + constraint);
            }
        } catch (Exception e) {
            log.error("Can not parse constraints '{}' of '{}'", constraintStr, containerSource, e);
        }
    }
}
