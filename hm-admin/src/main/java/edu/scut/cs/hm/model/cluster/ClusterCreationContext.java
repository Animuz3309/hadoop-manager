package edu.scut.cs.hm.model.cluster;

import edu.scut.cs.hm.model.ngroup.*;
import lombok.Data;
import org.springframework.util.Assert;

import java.util.function.Consumer;

/**
 * Create {@link AbstractNodesGroupConfig} of different type
 * {@link NodesGroupConfig#TYPE_DEFAULT}
 * {@link NodesGroupConfig#TYPE_SWARM}
 * {@link NodesGroupConfig#TYPE_DOCKER}
 *
 * And do something before cluster init {@link AbstractNodesGroup#init()}
 */
@Data
public class ClusterCreationContext {
    private final ClusterFactory factory;
    private final String cluster;
    private Consumer<AbstractNodesGroup<?>> beforeClusterInit;

    /**
     * Note: be careful when config loaded from storage - this cases must not
     * been validated, because user can not delete not loaded clusters.
     */
    private boolean mustValidated;

    public ClusterCreationContext(ClusterFactory factory, String cluster) {
        this.factory = factory;
        this.cluster = cluster;
    }

    public void beforeClusterInit(AbstractNodesGroup<?> cluster) {
        if (beforeClusterInit != null) {
            beforeClusterInit.accept(cluster);
        }
    }

    /**
     * Create config from config type
     * {@link NodesGroupConfig#TYPE_DEFAULT}
     * {@link NodesGroupConfig#TYPE_SWARM}
     * {@link NodesGroupConfig#TYPE_DOCKER}
     * @param type "DEFAULT" "SWARM" "DOCKER"
     * @return
     */
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
