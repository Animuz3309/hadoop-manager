package edu.scut.cs.hm.model.cluster;

import lombok.Builder;
import lombok.Data;

/**
 * State of nodes group
 */
@Data
@Builder(builderClassName = "Builder")
public class NodeGroupState {
    private final String message;
    private final boolean ok;
    private final boolean inited;
}
