package edu.scut.cs.hm.model.ngroup;

import edu.scut.cs.hm.common.kv.mapping.KvMapping;
import edu.scut.cs.hm.docker.DockerConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SwarmNodesGroupConfig extends AbstractNodesGroupConfig<SwarmNodesGroupConfig> implements DockerBasedClusterConfig {
    @KvMapping
    private DockerConfig config;
}
