package edu.scut.cs.hm.docker.model.container;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Used for Listing containers.
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Container {

    @JsonProperty("Command")
    private String command;

    @JsonProperty("Created")
    private Long created;

    @JsonProperty("Id")
    private String id;

    @JsonProperty("Image")
    private String image;

    @JsonProperty("ImageID")
    private String imageId;

    @JsonProperty("Names")
    private String[] names;

    @JsonProperty("Ports")
    public List<Port> ports;

    @JsonProperty("Labels")
    public Map<String, String> labels;

    @JsonProperty("Status")
    private String status;

    /**
     * See 'func (s *State) StateString()' at https://github.com/docker/docker/blob/master/container/state.go#L123 <p/>
     * Possible values: "paused", "restarting", "running", "removing", "dead", "created", "exited"
     */
    @JsonProperty("State")
    private String state;

    @JsonProperty("SizeRw")
    private Long sizeRw;

    @JsonProperty("SizeRootFs")
    private Long sizeRootFs;

    @JsonProperty("HostConfig")
    private ContainerHostConfig hostConfig;

    /**
     * Docker API docs says "list of networks", but json names `networkSettings`.
     * So, reusing existed NetworkSettings model object.
     *
     */
    @JsonProperty("NetworkSettings")
    private ContainerNetworkSettings networkSettings;

}
