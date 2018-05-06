package edu.scut.cs.hm.docker.cmd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import edu.scut.cs.hm.model.source.EditableContainerSource;
import lombok.Data;

@Data
public class UpdateContainerCmd {

    /**
     * Container ID
     */
    @JsonIgnore
    private String id;

    @JsonProperty("BlkioWeight")
    private Integer blkioWeight;

    @JsonProperty("CpuShares")
    private Integer cpuShares;

    @JsonProperty("CpuPeriod")
    private Integer cpuPeriod;

    @JsonProperty("CpuQuota")
    private Integer cpuQuota;

    @JsonProperty("CpusetCpus")
    private String cpusetCpus;

    @JsonProperty("CpusetMems")
    private String cpusetMems;

    @JsonProperty("Memory")
    private Long memory;

    @JsonProperty("MemorySwap")
    private Long memorySwap;

    @JsonProperty("MemoryReservation")
    private Long memoryReservation;

    @JsonProperty("KernelMemory")
    private Long kernelMemory;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("blkioWeight", blkioWeight)
                .add("cpuShares", cpuShares)
                .add("cpuPeriod", cpuPeriod)
                .add("cpuQuota", cpuQuota)
                .add("cpusetCpus", cpusetCpus)
                .add("cpusetMems", cpusetMems)
                .add("memory", memory)
                .add("memorySwap", memorySwap)
                .add("memoryReservation", memoryReservation)
                .add("kernelMemory", kernelMemory)
                .omitNullValues()
                .toString();
    }

    /**
     * Copy parameters from source to this
     * @param src source
     * @return this
     */
    public UpdateContainerCmd from(EditableContainerSource src) {
        setBlkioWeight(src.getBlkioWeight());
        setCpuPeriod(src.getCpuPeriod());
        setCpuQuota(src.getCpuQuota());
        setCpuShares(src.getCpuShares());
        setCpusetCpus(src.getCpusetCpus());
        setCpusetMems(src.getCpusetMems());
        setKernelMemory(src.getKernelMemory());
        setMemory(src.getMemoryLimit());
        setMemoryReservation(src.getMemoryReservation());
        setMemorySwap(src.getMemorySwap());
        return this;
    }
}
