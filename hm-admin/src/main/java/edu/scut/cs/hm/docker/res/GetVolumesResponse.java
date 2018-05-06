package edu.scut.cs.hm.docker.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.scut.cs.hm.docker.model.volume.Volume;
import lombok.Data;

import java.util.List;

@Data
public class GetVolumesResponse {

    @JsonProperty("Volumes")
    private final List<Volume> volumes;

    @JsonProperty("Warnings")
    private final List<String> warnings;
}
