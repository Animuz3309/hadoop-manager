package edu.scut.cs.hm.docker.model.container;

import com.fasterxml.jackson.annotation.*;
import edu.scut.cs.hm.docker.model.backend.Capability;
import edu.scut.cs.hm.docker.model.backend.Ulimit;
import edu.scut.cs.hm.docker.model.mount.Mount;
import edu.scut.cs.hm.docker.model.network.Link;
import edu.scut.cs.hm.docker.model.network.Links;
import edu.scut.cs.hm.docker.model.network.Ports;
import edu.scut.cs.hm.docker.model.storage.Device;
import edu.scut.cs.hm.docker.model.storage.VolumesFrom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * Used in `/containers/create`, and in inspect container.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor(onConstructor = @__(@JsonCreator))
@Builder
public class HostConfig {

    @JsonProperty("Binds")
    private List<String> binds;

    @JsonProperty("BlkioWeight")
    private Integer blkioWeight;

    @JsonProperty("BlkioWeightDevice")
    private List<Object> blkioWeightDevice;

    @JsonProperty("BlkioDeviceReadBps")
    private List<Object> blkioDeviceReadBps;

    @JsonProperty("BlkioDeviceReadIOps")
    private List<Object> blkioDeviceReadIOps;

    @JsonProperty("BlkioDeviceWriteBps")
    private List<Object> blkioDeviceWriteBps;

    @JsonProperty("BlkioDeviceWriteIOps")
    private List<Object> blkioDeviceWriteIOps;

    @JsonProperty("MemorySwappiness")
    private Integer memorySwappiness;

    @JsonProperty("CapAdd")
    private Capability[] capAdd;

    @JsonProperty("CapDrop")
    private Capability[] capDrop;

    @JsonProperty("ContainerIDFile")
    private String containerIDFile;

    @JsonProperty("CpuPeriod")
    private Integer cpuPeriod;

    @JsonProperty("CpuShares")
    private Integer cpuShares;

    @JsonProperty("CpuQuota")
    private Integer cpuQuota;

    @JsonProperty("CpusetCpus")
    private String cpusetCpus;

    @JsonProperty("CpusetMems")
    private String cpusetMems;

    @JsonProperty("Devices")
    private Device[] devices;

    @JsonProperty("Dns")
    private List<String> dns;

    @JsonProperty("DnsSearch")
    private List<String> dnsSearch;

    @JsonProperty("ExtraHosts")
    private List<String> extraHosts;

    @JsonProperty("Links")
    private Links links;

    @JsonProperty("LogConfig")
    private LogConfig logConfig;

    @JsonProperty("LxcConf")
    private LxcConfig[] lxcConfig;

    @JsonProperty("Memory")
    private Long memory;

    @JsonProperty("MemorySwap")
    private Long memorySwap;

    @JsonProperty("MemoryReservation")
    private Long memoryReservation;

    @JsonProperty("KernelMemory")
    private Long kernelMemory;

    @JsonProperty("NetworkMode")
    private String networkMode;

    @JsonProperty("OomKillDisable")
    private Boolean oomKillDisable;

    @JsonProperty("OomScoreAdj")
    private Boolean oomScoreAdj;

    @JsonProperty("PortBindings")
    private Ports portBindings;

    @JsonProperty("Privileged")
    private Boolean privileged;

    @JsonProperty("PublishAllPorts")
    private Boolean publishAllPorts;

    @JsonProperty("ReadonlyRootfs")
    private Boolean readonlyRootfs;

    @JsonProperty("RestartPolicy")
    private RestartPolicy restartPolicy;

    @JsonProperty("Ulimits")
    private Ulimit[] ulimits;

    @JsonProperty("VolumesFrom")
    private VolumesFrom[] volumesFrom;

    @JsonProperty("PidMode")
    private String pidMode;

    @JsonProperty("SecurityOpt")
    private List<String> securityOpts;

    @JsonProperty("CgroupParent")
    private String cgroupParent;

    @JsonProperty("VolumeDriver")
    private String volumeDriver;

    /**
     * Usually used in creation. In 'inspect' may be null.
     */
    @JsonProperty("Mounts")
    private List<Mount> mounts;

    @JsonProperty("ShmSize")
    private String shmSize;

    @JsonIgnore
    public List<String> getBinds() {
        return (binds == null) ? Collections.emptyList() : binds;
    }

    @JsonIgnore
    public List<Link> getLinks() {
        return (links == null) ? Collections.emptyList() : links.getLinks();
    }

    @JsonIgnore
    public LogConfig getLogConfig() {
        return (logConfig == null) ? new LogConfig() : logConfig;
    }

}