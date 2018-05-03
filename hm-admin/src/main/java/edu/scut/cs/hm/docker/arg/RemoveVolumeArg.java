package edu.scut.cs.hm.docker.arg;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class RemoveVolumeArg {

    /**
     * Volume name or id
     */
    @JsonIgnore
    private String name;

    /**
     * Force the removal of the volume
     */
    private Boolean force;

}
