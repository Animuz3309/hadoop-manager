package edu.scut.cs.hm.model.node;
import edu.scut.cs.hm.docker.DockerServiceAddress;
import edu.scut.cs.hm.model.Named;

/**
 * Represent physical docker node
 */
public interface Node extends Named, DockerServiceAddress {

    /**
     * Name of node. It can be host name.
     * @return name of node
     */
    String getName();
}
