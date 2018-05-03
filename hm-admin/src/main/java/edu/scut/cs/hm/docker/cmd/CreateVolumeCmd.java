package edu.scut.cs.hm.docker.cmd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 */
@Data
public class CreateVolumeCmd {
    /**
     * The new volume's name. If not specified, Docker generates a name.
     */
    @JsonProperty("Name")
    private String name;

    /**
     * Name of the volume driver to use.
     * Default 'local'.
     */
    @JsonProperty("Driver")
    private String driver;

    /**
     * A mapping of driver options and values. These options are passed directly to the driver and are driver specific.
     */
    @JsonProperty("DriverOpts")
    private Map<String, String> driverOpts;

    /**
     * User-defined key/value metadata.
     */
    @JsonProperty("Labels")
    private Map<String, String> labels;


}
