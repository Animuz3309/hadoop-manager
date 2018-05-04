package edu.scut.cs.hm.model.filter;

import edu.scut.cs.hm.admin.component.FilterFactory;
import edu.scut.cs.hm.admin.security.TempAuth;
import edu.scut.cs.hm.model.cluster.ClusterUtils;
import edu.scut.cs.hm.model.ngroup.NodesGroup;
import edu.scut.cs.hm.model.cluster.DiscoveryStorage;
import edu.scut.cs.hm.model.node.NodeRegistration;
import org.springframework.util.StringUtils;

public class OrphansNodeFilterFactory implements FilterFactory.Factory {

    private static final String PROTO = "nodes-orphan";
    public static final String FILTER = PROTO + ":";

    private final DiscoveryStorage ds;

    public OrphansNodeFilterFactory(DiscoveryStorage ds) {
        this.ds = ds;
    }

    @Override
    public Filter create(String expr) {
        return (o) -> {
            String cluster = ((NodeRegistration) o).getNodeInfo().getCluster();
            if(!StringUtils.hasText(cluster)) {
                return true;
            }
            //also we want see nodes which ngroup has been deleted
            try(TempAuth ta = TempAuth.asSystem()) {
                NodesGroup group = ds.getCluster(cluster);
                return group == null || !ClusterUtils.isDockerBased(group);
            }
        };
    }

    @Override
    public String getProtocol() {
        return PROTO;
    }
}
