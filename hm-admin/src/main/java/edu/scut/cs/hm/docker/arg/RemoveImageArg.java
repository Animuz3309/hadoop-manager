package edu.scut.cs.hm.docker.arg;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RemoveImageArg {

    /**
     * You can use here image id like 'sha256:...' or image name 'name:tag'.
     */
    private final String imageId;
    private final String cluster;
    private final Boolean force;
    private final Boolean noPrune;

}
