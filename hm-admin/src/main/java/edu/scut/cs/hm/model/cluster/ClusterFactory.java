package edu.scut.cs.hm.model.cluster;

import edu.scut.cs.hm.admin.service.DiscoveryStorageImpl;
import edu.scut.cs.hm.common.kv.mapping.KvMapperFactory;
import edu.scut.cs.hm.docker.DockerConfig;
import edu.scut.cs.hm.model.ngroup.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.util.Assert;

/**
 * Create ngroup which means {@link edu.scut.cs.hm.model.ngroup.NodesGroup}
 */
@Data
@Slf4j
public class ClusterFactory {
    private final DiscoveryStorageImpl storage;
    private AbstractNodesGroupConfig<?> config;
    private String type;
    private ClusterConfigFactory configFactory;
    private KvMapperFactory kvmf;
    private final AutowireCapableBeanFactory beanFactory;

    public ClusterFactory config(AbstractNodesGroupConfig<?> config) {
        setConfig(config);
        return this;
    }

    public ClusterFactory configFactory(ClusterConfigFactory consumer) {
        setConfigFactory(consumer);
        return this;
    }

    public NodesGroup build(String clusterId) {
        ClusterCreationContext ccc = new ClusterCreationContext(this, clusterId);
        processConfig(ccc);
        AbstractNodesGroup<?> cluster;
        if (config instanceof DefaultNodesGroupConfig) {
            cluster = DefaultCluster.builder()
                    .config((DefaultNodesGroupConfig) config)
                    .storage(storage)
                    .build();
        } else if(config instanceof SwarmNodesGroupConfig) {
            SwarmNodesGroupConfig localConfig = (SwarmNodesGroupConfig) config;
            cluster = new SwarmCluster(storage, localConfig);
        } else if(config instanceof DockerClusterConfig) {
            DockerClusterConfig localConfig = (DockerClusterConfig) config;
            cluster = new DockerCluster(storage, localConfig);
        } else {
            throw new IllegalArgumentException("Unsupported type of cluster config: " + config.getClass());
        }
    }

    private void processConfig(ClusterCreationContext ccc) {
        Assert.isTrue(type != null || config != null || configFactory != null,
                "Both 'type' and 'config' is null, we can not resolve type of created cluster.");
        if (config == null) {
            if(configFactory != null) {
                config = configFactory.create(ccc);
                Assert.notNull(config, "Config factory: " + configFactory + " return null.");
            } else {
                config = ccc.createConfig(getType());
            }
        }
        if(config instanceof DockerBasedClusterConfig) {
            fixConfig((DockerBasedClusterConfig)config);
        }
    }

    private void fixConfig(DockerBasedClusterConfig localConfig) {
        if(localConfig.getConfig() != null) {
            return;
        }
        log.warn("Configuration of cluster '{}' contain null value of {}, set default instance. ",
                localConfig.getName(), DockerConfig.class);
        initDefaultConfig(localConfig);
    }

    void initDefaultConfig(DockerBasedClusterConfig localConfig) {
        localConfig.setConfig(DockerConfig.builder().cluster(localConfig.getName()).build());
    }
}
