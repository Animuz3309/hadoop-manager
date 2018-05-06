package edu.scut.cs.hm.model.source;

import lombok.Data;

/**
 * Part of ContainerSource which has updatable properties.
 */
@Data
public class EditableContainerSource implements Cloneable {


    private Integer cpuShares;
    /**
     * 'limit-cpu'
     */
    private Integer cpuQuota;
    /**
     * By default, all containers get the same proportion of block IO bandwidth (blkioWeight). This proportion is 500.
     * To modify this proportion, change the container’s blkioWeight weight relative to the weighting of all other running containers.
     * The flag can set the weighting to a value between 10 to 1000.
     */
    private Integer blkioWeight;

    /**
     * 'reserve-cpu'
     */
    private Integer cpuPeriod;
    /**
     * Set cpu numbers in which to allow execution for containers. <p/>
     * <code>"1,3"</code> - executed on cpu 1 and cpu 3 <p/>
     * <code>"0-2"</code> - executed on cpu 0, cpu 1 and cpu 2. <p/>
     */
    private String cpusetCpus;
    /**
     * Set cpu numbers in which to allow execution for containers. <p/>
     * Memory nodes (MEMs) in which to allow execution (0-3, 0,1). Only effective on NUMA systems.
     * <code>"1,3"</code> - executed on cpu 1 and cpu 3 <p/>
     * <code>"0-2"</code> - executed on cpu 0, cpu 1 and cpu 2. <p/>
     */
    private String cpusetMems;
    /**
     * Restart policy to apply when a container exits (default 'no') <p/>
     * Possible values are : 'no', 'on-failure'[':'max-retry], 'always', 'unless-stopped'
     */
    private String restart;

    private Long memoryLimit;
    private Long memorySwap;
    private Long memoryReservation;
    private Long kernelMemory;

    @Override
    public EditableContainerSource clone() {
        try {
            return (EditableContainerSource) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

}
