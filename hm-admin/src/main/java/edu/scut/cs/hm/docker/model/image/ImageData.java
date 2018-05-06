package edu.scut.cs.hm.docker.model.image;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.scut.cs.hm.docker.model.container.ContainerConfig;
import lombok.Data;

import java.util.Date;

@Data
public class ImageData {
    private String architecture;
    private String author;
    private ContainerConfig config;
    @JsonProperty("container_config")
    private ContainerConfig containerConfig;
    private Date created;
    @JsonProperty("docker_version")
    private String dockerVersion;
    private String os;
    // we do not need this yet
    //private List<HistoryEntry> history;

}
