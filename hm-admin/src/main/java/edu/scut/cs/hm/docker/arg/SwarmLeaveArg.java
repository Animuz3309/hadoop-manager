package edu.scut.cs.hm.docker.arg;

import lombok.Data;

/**
 */
@Data
public class SwarmLeaveArg {

    /**
     * 'Force leave swarm, even if this is the last manager or that it will break the ngroup.' <p/>
     * Default 'false'.
     */
    private Boolean force;
}
