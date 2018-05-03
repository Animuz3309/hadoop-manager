package edu.scut.cs.hm.docker.res;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 */
public class ServiceUpdateResult extends ServiceCallResult {

    /**
     * The image ID of an image that was untagged
     */
    @JsonProperty("Untagged")
    private String untagged;

    /**
     * The image ID of an image that was deleted
     */
    @JsonProperty("Deleted")
    private String deleted;
}
