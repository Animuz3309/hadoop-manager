package edu.scut.cs.hm.model.ngroup;

import edu.scut.cs.hm.docker.DockerConfig;

/**
 * A ngroup of nodes group united by swarm or docker in swarm mode
 */
public interface DockerBasedClusterConfig extends NodesGroupConfig {
    DockerConfig getConfig();
    void setConfig(DockerConfig config);
}
