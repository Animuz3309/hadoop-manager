package edu.scut.cs.hm.model;

/**
 * Attributes of docker service. Also may used for swarm and other docker-like services.
 */
public interface DockerServiceAddress {
    String getAddress();
}
