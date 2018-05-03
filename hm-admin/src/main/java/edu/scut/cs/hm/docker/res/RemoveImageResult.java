package edu.scut.cs.hm.docker.res;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Result of call service
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RemoveImageResult extends ServiceCallResult {

    private String image;

}
