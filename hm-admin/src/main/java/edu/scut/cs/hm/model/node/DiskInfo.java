package edu.scut.cs.hm.model.node;

import lombok.Data;

/**
 * Information of disk on swarmNode
 */
@Data
public class DiskInfo {
    /**
     * mount point
     */
    private final String mount;
    private final long used;
    private final long total;
}
