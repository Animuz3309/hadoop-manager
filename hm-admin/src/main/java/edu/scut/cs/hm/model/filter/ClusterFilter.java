package edu.scut.cs.hm.model.filter;

import edu.scut.cs.hm.model.WithCluster;

/**
 * Filter which pass objects with `pattern.equals(this.ngroup)`.
 * @see WithCluster
 */
public class ClusterFilter implements Filter {

    public static final String PROTO = "ngroup";
    private final String pattern;
    private final String expr;

    public ClusterFilter(String pattern) {
        this.pattern = pattern;
        this.expr = PROTO + ":" + pattern;
    }

    @Override
    public boolean test(Object o) {
        String cluster = null;
        if(o instanceof WithCluster) {
            cluster = ((WithCluster) o).getCluster();
        }
        //here we can add some other ways to extract name of ngroup
        return pattern.equals(cluster);
    }

    @Override
    public String getExpression() {
        return expr;
    }
}
