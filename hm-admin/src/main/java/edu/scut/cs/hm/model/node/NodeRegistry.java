package edu.scut.cs.hm.model.node;

import edu.scut.cs.hm.docker.DockerService;

/**
 * Get node's Docker Service
 */
public interface NodeRegistry {

    /**
     * Docker service of node
     * @param name
     * @return
     */
    DockerService getNodeDockerService(String name);
}
