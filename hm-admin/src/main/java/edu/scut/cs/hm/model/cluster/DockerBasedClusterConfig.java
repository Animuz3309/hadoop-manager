package edu.scut.cs.hm.model.cluster;

import edu.scut.cs.hm.docker.DockerConfig;

/**
 * A cluster of nodes group united by swarm or docker in swarm mode
 */
public interface DockerBasedClusterConfig extends NodesGroupConfig {
    DockerConfig getConfig();
    void setConfig(DockerConfig config);
}
