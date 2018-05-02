package edu.scut.cs.hm.model.node;
import edu.scut.cs.hm.docker.DockerServiceAddress;
import edu.scut.cs.hm.model.Named;

/**
 * Represent physical docker swarmNode
 */
public interface Node extends Named, DockerServiceAddress {

    /**
     * Name of swarmNode. It can be host name.
     * @return name of swarmNode
     */
    String getName();
}
