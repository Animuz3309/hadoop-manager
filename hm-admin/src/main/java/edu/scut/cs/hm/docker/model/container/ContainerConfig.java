package edu.scut.cs.hm.docker.model.container;

import com.fasterxml.jackson.annotation.*;
import edu.scut.cs.hm.docker.model.network.ExposedPort;
import edu.scut.cs.hm.docker.model.network.ExposedPorts;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Configuration of image container.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@AllArgsConstructor(onConstructor = @__(@JsonCreator))
public class ContainerConfig {

    @JsonProperty("AttachStderr")
    private final Boolean attachStderr;

    @JsonProperty("AttachStdin")
    private final Boolean attachStdin;

    @JsonProperty("AttachStdout")
    private final Boolean attachStdout;

    @JsonProperty("Cmd")
    private final List<String> cmd;

    @JsonProperty("Domainname")
    private final String domainName;

    @JsonProperty("Entrypoint")
    private final List<String> entrypoint;

    @JsonProperty("Env")
    private final List<String> env;

    @JsonProperty("ExposedPorts")
    private final ExposedPorts exposedPorts;

    @JsonProperty("Hostname")
    private final String hostName;

    @JsonProperty("Image")
    private final String image;

    @JsonProperty("Labels")
    private final Map<String, String> labels;

    @JsonProperty("MacAddress")
    private final String macAddress;

    @JsonProperty("NetworkDisabled")
    private final Boolean networkDisabled;

    @JsonProperty("OnBuild")
    private final List<String> onBuild;

    @JsonProperty("OpenStdin")
    private final Boolean stdinOpen;

    @JsonProperty("PortSpecs")
    private final List<String> portSpecs;

    @JsonProperty("StdinOnce")
    private final Boolean stdInOnce;

    @JsonProperty("Tty")
    private final Boolean tty;

    @JsonProperty("User")
    private final String user;

    @JsonProperty("Volumes")
    private final Map<String, ?> volumes;

    @JsonProperty("WorkingDir")
    private final String workingDir;

    @JsonIgnore
    public List<ExposedPort> getExposedPorts() {
        return exposedPorts != null ? exposedPorts.getExposedPorts() : null;
    }
}
