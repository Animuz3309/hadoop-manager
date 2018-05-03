package edu.scut.cs.hm.docker.model.container;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

/**
 * State of container.
 */
@Data
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class ContainerState {

    @JsonProperty("Running")
    private boolean running;
    @JsonProperty("Paused")
    private boolean paused;
    @JsonProperty("Restarting")
    private boolean restarting;
    @JsonProperty("Pid")
    private int pid;
    @JsonProperty("ExitCode")
    private int exitCode;
    @JsonProperty("StartedAt")
    private Date startedAt;
    @JsonProperty("FinishedAt")
    private Date finishedAt;
    @JsonProperty("Error")
    private String error;
    @JsonProperty("Status")
    private String status;
    @JsonProperty("OOMKilled")
    private boolean oomKilled;
}
