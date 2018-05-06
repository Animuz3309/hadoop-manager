package edu.scut.cs.hm.docker.mng;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

@Data
public class Info {

    @JsonProperty("Id")
    private String id;

    @JsonProperty("SystemTime")
    private ZonedDateTime systemTime;

    @JsonProperty("Containers")
    private Integer containers;
    @JsonProperty("Images")
    private Integer images;
    @JsonProperty("NCPU")
    private Integer ncpu;
    @JsonProperty("MemTotal")
    private long memory;
    @JsonProperty("Name")
    private String name;

    @JsonProperty("DriverStatus")
    private List<List<String>> driverStatus;
    @JsonProperty("SystemStatus")
    private List<List<String>> systemStatus;

    @JsonProperty("Labels")
    private List<String> labels;

    @JsonProperty("Swarm")
    private InfoSwarm swarm;
}
