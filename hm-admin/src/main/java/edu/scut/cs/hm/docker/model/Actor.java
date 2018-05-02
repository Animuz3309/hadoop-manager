package edu.scut.cs.hm.docker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * Actor describes something that generates events
 * <a href="https://github.com/moby/moby/blob/master/docs/api/v1.24.md#Monitor Docker's events">Docker api#Monitor Docker's events</a>
 */
@Data
public class Actor {

    @JsonProperty("ID")
    private String id;

    @JsonProperty("Attributes")
    private Map<String, String> attributes;
}
