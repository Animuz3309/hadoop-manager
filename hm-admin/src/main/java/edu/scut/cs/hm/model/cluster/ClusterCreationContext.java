package edu.scut.cs.hm.model.cluster;

import edu.scut.cs.hm.model.ngroup.*;
import lombok.Data;
import org.springframework.util.Assert;

import java.util.function.Consumer;

@Data
public class ClusterCreationContext {
    private final ClusterFactory factory;
    private final String cluster;
    private Consumer<AbstractNodesGroup<?>> beforeClusterInit;

    public ClusterCreationContext(ClusterFactory factory, String cluster) {
        this.factory = factory;
        this.cluster = cluster;
    }

    public void beforeClusterInit(AbstractNodesGroup<?> cluster) {
        if (beforeClusterInit != null) {
            beforeClusterInit.accept(cluster);
        }
    }

    public AbstractNodesGroupConfig<?> createConfig(String type) {
        Assert.notNull(type, "type is null");
        AbstractNodesGroupConfig<?> config;
        switch (type) {
            case NodesGroupConfig.TYPE_DEFAULT: {
                config = new DefaultNodesGroupConfig();
                break;
            }
            case NodesGroupConfig.TYPE_SWARM: {
                config = new SwarmNodesGroupConfig();
                break;
            }
            case NodesGroupConfig.TYPE_DOCKER: {
                config = new DockerClusterConfig();
                break;
            }
            default:
                throw new IllegalArgumentException("Unsupported type of ngroup: " + type);
        }
        factory.initDefaultConfig((DockerBasedClusterConfig) config);
        config.setName(getCluster());
        return config;
    }
}
