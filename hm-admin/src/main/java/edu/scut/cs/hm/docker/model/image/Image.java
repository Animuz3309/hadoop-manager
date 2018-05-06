package edu.scut.cs.hm.docker.model.image;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.scut.cs.hm.docker.model.container.ContainerConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * Represent Image inside Manifest DTO -> docker dto
 */
@EqualsAndHashCode
@Data
@AllArgsConstructor(onConstructor = @__(@JsonCreator))
@Builder
public class Image implements ImageDescriptor {


    @JsonProperty("Created")
    private final Date created;
    @JsonProperty("Id")
    private final String id;
    @JsonProperty("Parent")
    private final String parent;
    @JsonProperty("RepoTags")
    private final List<String> repoTags;
    @JsonProperty("ContainerConfig")
    private final ContainerConfig containerConfig;
    @JsonProperty("Size")
    private final long size;
    @JsonProperty("VirtualSize")
    private final long virtualSize;

}
