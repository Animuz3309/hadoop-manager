package edu.scut.cs.hm.docker.arg;

import lombok.Data;

/**
 * Whether get all containers in docker
 */
@Data
public class GetContainersArg {
    private final boolean all;
}
