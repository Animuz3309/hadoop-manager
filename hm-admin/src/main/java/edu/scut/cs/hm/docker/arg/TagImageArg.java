package edu.scut.cs.hm.docker.arg;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TagImageArg {

    /**
     * image name w/o version
     */
    private final String imageName;
    private final String cluster;
    private final String repository;
    /**
     * new tag
     */
    private final String newTag;
    private final String currentTag;
    private final Boolean force;
    private final Boolean remote;

}
