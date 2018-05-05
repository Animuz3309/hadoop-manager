package edu.scut.cs.hm.model.container;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.*;

@Slf4j
public final class ContainerStarterHelper {

    private static final String CONSTRAINT_NODE = "constraint:node";

    private static List<NodePriority> processServicesCount(Map<String, Integer> appCountPerNode,
                                                           Collection<String> existsNodes) {
        List<NodePriority> values = new ArrayList<>();
        for (String n : existsNodes) {
            Integer count = appCountPerNode.get(n);
            if (count == null) {
                count = 0;
            }
            values.add(new NodePriority(n, count));
        }
        Collections.sort(values);
        log.info("Service count by nodes {}", values);
        return values;
    }

    /**
     * Calculate constraints for container
     * 1. we do not allow to start N same containers on one node
     * 2. we set node with min count of services as preferred (soft constraint)
     *
     * @param existsNodes
     * @param node
     * @param appCountPerNode
     * @param maxCount
     * @return
     */
    public static void calculateConstraints(Collection<String> existsNodes,
                                            String node,
                                            Map<String, Integer> appCountPerNode,
                                            int maxCount,
                                            Collection<String> dest) {

        if (StringUtils.hasText(node)) {
            // specified node has higher priority
            dest.removeIf(i -> i.startsWith(CONSTRAINT_NODE));
            dest.add(CONSTRAINT_NODE + "==" + node);
        }

        if (dest.stream().anyMatch(i -> i.startsWith(CONSTRAINT_NODE))) {
            return;
        }

        if (appCountPerNode == null || appCountPerNode.isEmpty()) {
            return;
        }

        List<NodePriority> values = processServicesCount(appCountPerNode, existsNodes);

        String preferred = calculatePreferredNodeExpression(values, maxCount);
        dest.add(preferred);
        String forbidden = calculateFullNodeExpression(getFullNodes(values, maxCount));
        if (forbidden != null) {
            dest.add(forbidden);
        }
    }

    private static String calculateFullNodeExpression(List<NodePriority> fullNodes) {
        if (!fullNodes.isEmpty()) {
            StringBuilder nodes = new StringBuilder(CONSTRAINT_NODE + "!=/");
            for (int i = 0; i < fullNodes.size(); i++) {
                if (i > 0) {
                    nodes.append("|");
                }
                // \Q...\E - quote sequence between them
                nodes.append("\\\\Q").append(fullNodes.get(i).name).append("\\\\E");
            }
            nodes.append("/");
            return nodes.toString();
        }
        return null;
    }

    private static String calculatePreferredNodeExpression(List<NodePriority> values, int maxCount) {
        NodePriority nodePriority = values.get(0);
        if (nodePriority.countOfApps > maxCount) {
            throw new IllegalArgumentException("Can't schedule container, all nodes contains at least " + maxCount);
        }
        String cs = CONSTRAINT_NODE + "==~" + nodePriority.name;
        log.info("Preferred node: {}", cs);
        return cs;
    }

    private static List<NodePriority> getFullNodes(List<NodePriority> values, int max) {
        if (max < 1) {
            return Collections.emptyList();
        }
        List<NodePriority> result = new ArrayList<>();
        for (NodePriority value : values) {
            if (value.countOfApps >= max) {
                result.add(value);
            }
        }
        if (!result.isEmpty()) {
            log.info("Full nodes: {}", result);
        }
        return result;
    }

    private static final class NodePriority implements Comparable<NodePriority> {

        private final int countOfApps;
        private final String name;

        public NodePriority(String name, int countOfApps) {
            this.name = name;
            this.countOfApps = countOfApps;
        }

        @Override
        public int compareTo(NodePriority o) {
            return Integer.compare(countOfApps, o.countOfApps);
        }

        @Override
        public String toString() {
            return "NodePriority{" +
                    "countOfApps=" + countOfApps +
                    ", name='" + name + '\'' +
                    '}';
        }
    }


}
