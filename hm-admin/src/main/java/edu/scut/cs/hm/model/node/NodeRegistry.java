package edu.scut.cs.hm.model.node;

import edu.scut.cs.hm.docker.DockerService;

/**
 * Get swarmNode's Docker Service
 */
public interface NodeRegistry {

    /**
     * Docker service of swarmNode
     * @param name
     * @return
     */
    DockerService getNodeDockerService(String name);
}
