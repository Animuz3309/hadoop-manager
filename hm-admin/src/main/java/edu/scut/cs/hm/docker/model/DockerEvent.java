package edu.scut.cs.hm.docker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Representation of a Docker event.
 * <a href="https://github.com/moby/moby/blob/master/docs/api/v1.24.md#Monitor Docker's events">Docker api#Monitor Docker's events</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class DockerEvent {

    /**
     * Status of docker image or container. List of statuses is available in Docker api#Monitor Docker's events
     */
    private String status;

    /**
     * ID of docker container
     */
    private String id;

    /**
     * Source image of the container.
     */
    private String from;

    private long time;

    /**
     * Returns the swarmNode when working against docker swarm
     */
    private SwarmNode swarmNode;

    /**
     * Event type. List of statuses is available in Docker api#Monitor Docker's events
     */
    @JsonProperty("Type")
    private DockerEventType type;

    /**
     * Event action. List of statuses is available in Docker api#Monitor Docker's events
     */
    @JsonProperty("Action")
    private String action;

    /**
     *  Actor describes something that generates events
     */
    @JsonProperty("Actor")
    private Actor actor;
}