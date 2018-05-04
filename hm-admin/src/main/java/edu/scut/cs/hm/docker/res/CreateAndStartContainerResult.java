package edu.scut.cs.hm.docker.res;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CreateAndStartContainerResult extends ServiceCallResult {
    /**
     * Id of created container
     * @return
     */
    private String containerId;
    /**
     * Name of created container.
     */
    private String name;
}
