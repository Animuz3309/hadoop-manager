package edu.scut.cs.hm.docker.arg;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

/**
 * Delete Container {@link edu.scut.cs.hm.docker.DockerService#deleteContainer(DeleteContainerArg)}
 */
@Data
@Builder(builderClassName = "Builder")
public class DeleteContainerArg {

    /**
     * Container ID
     */
    @JsonIgnore
    private final String id;

    /**
     * Remove the volumes associated to the container. Default false.
     */
    private final boolean deleteVolumes;
    /**
     * Kill container if it run. Default false.
     */
    private final boolean kill;

}

