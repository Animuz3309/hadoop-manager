package edu.scut.cs.hm.docker.arg;

import lombok.Data;

@Data
public class RemoveNodeArg {
    private Boolean force;
    /**
     * Node id from swarm mode of docker. <p/>
     * Not confuse it with node name or address, it must be string like '24ifsmvkjbyhk'.
     */
    private String nodeId;

    public RemoveNodeArg() {

    }

    public RemoveNodeArg(String nodeId) {
        setNodeId(nodeId);
    }

    public RemoveNodeArg force(Boolean force) {
        setForce(force);
        return this;
    }
}
