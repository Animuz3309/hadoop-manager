package edu.scut.cs.hm.docker.model.container;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.scut.cs.hm.common.utils.StringUtils;
import edu.scut.cs.hm.docker.model.mount.Mount;
import edu.scut.cs.hm.docker.model.network.NetworkSettings;
import edu.scut.cs.hm.docker.model.SwarmNode;
import edu.scut.cs.hm.model.ContainerBaseIface;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Detailed info for container
 * https://github.com/moby/moby/blob/51a9119f6b817bbae21805ec05787d462c9492cd/api/types/types.go
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ContainerDetails implements ContainerBaseIface {

    @JsonProperty("Args")
    private List<String> args;

    @JsonProperty("Config")
    private ContainerConfig config;

    @JsonProperty("Created")
    private Date created;

    @JsonProperty("Driver")
    private String driver;

    @JsonProperty("ExecDriver")
    private String execDriver;

    @JsonProperty("HostConfig")
    private HostConfig hostConfig;

    @JsonProperty("HostnamePath")
    private String hostnamePath;

    @JsonProperty("HostsPath")
    private String hostsPath;

    @JsonProperty("Id")
    private String id;

    @JsonProperty("Image")
    private String imageId;

    @JsonProperty("MountLabel")
    private String mountLabel;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("NetworkSettings")
    private NetworkSettings networkSettings;

    @JsonProperty("RestartCount")
    private Integer restartCount;

    @JsonProperty("Path")
    private String path;

    @JsonProperty("ProcessLabel")
    private String processLabel;

    @JsonProperty("ResolvConfPath")
    private String resolvConfPath;

    @JsonProperty("ExecIDs")
    private List<String> execIds;

    @JsonProperty("State")
    private ContainerState state;

    @JsonProperty("Mounts")
    private List<MountPoint> mounts;

    @JsonProperty("Node")
    private SwarmNode node;

    @Override
    public String getImage() {
        return config.getImage();
    }

    @Override
    public Map<String, String> getLabels() {
        return config.getLabels();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class MountPoint {

        @JsonProperty("Type")
        private Mount.Type type;

        @JsonProperty("Name")
        private String name;

        @JsonProperty("Source")
        private String source;

        @JsonProperty("Destination")
        private String destination;

        @JsonProperty("Driver")
        private String driver;

        @JsonProperty("Mode")
        private String mode;

        @JsonProperty("RW")
        private boolean rw;

        @JsonProperty("Propagation")
        private final Mount.Propagation propagation;

        public boolean isSystem() {
            return name != null && name.trim().length() == 64 && StringUtils.matchHex(name);
        }
    }
}
