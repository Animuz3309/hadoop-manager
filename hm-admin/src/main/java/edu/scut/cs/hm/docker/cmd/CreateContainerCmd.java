package edu.scut.cs.hm.docker.cmd;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.scut.cs.hm.docker.model.container.HostConfig;
import edu.scut.cs.hm.docker.model.network.ExposedPorts;
import lombok.Data;

import java.util.Map;

/**
 * https://github.com/moby/moby/blob/e2d3bb305258b78beb8e8f97a35d28cc6a75ac3c/docs/api/v1.24.md
 * Create Container Rest model
 */
@Data
public class CreateContainerCmd {

    private String name;

    @JsonProperty("Hostname")
    private String hostName;

    @JsonProperty("Domainname")
    private String domainName;

    @JsonProperty("User")
    private String user;

    @JsonProperty("AttachStdin")
    private Boolean attachStdin;

    @JsonProperty("AttachStdout")
    private Boolean attachStdout;

    @JsonProperty("AttachStderr")
    private Boolean attachStderr;

    @JsonProperty("PortSpecs")
    private String[] portSpecs;

    @JsonProperty("Tty")
    private Boolean tty;

    @JsonProperty("OpenStdin")
    private Boolean stdinOpen;

    @JsonProperty("StdinOnce")
    private Boolean stdInOnce;

    @JsonProperty("Env")
    private String[] env;

    @JsonProperty("Cmd")
    private String[] cmd;

    @JsonProperty("Entrypoint")
    private String[] entrypoint;

    @JsonProperty("Image")
    private String image;

    /**
     * An object mapping mount point paths inside the container to empty objects.<p/>
     * <pre>
     *     "Volumes":{
     "/volumes/data": { }
     },
     * </pre>
     */
    @JsonProperty("Volumes")
    private Map<String, Object> volumes;

    @JsonProperty("WorkingDir")
    private String workingDir;

    @JsonProperty("MacAddress")
    private String macAddress;

    @JsonProperty("NetworkDisabled")
    private Boolean networkDisabled;

    @JsonProperty("ExposedPorts")
    private ExposedPorts exposedPorts;

    @JsonProperty("HostConfig")
    private HostConfig hostConfig;

    @JsonProperty("Labels")
    private Map<String, String> labels;

}
