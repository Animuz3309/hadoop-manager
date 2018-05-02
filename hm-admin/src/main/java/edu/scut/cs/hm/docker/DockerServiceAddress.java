package edu.scut.cs.hm.docker;

/**
 * Attributes of docker service. Also may used for swarm and other docker-like services.
 */
public interface DockerServiceAddress {
    String getAddress();
}
