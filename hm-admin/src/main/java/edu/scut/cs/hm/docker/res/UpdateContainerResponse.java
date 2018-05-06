package edu.scut.cs.hm.docker.res;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class UpdateContainerResponse {

    @JsonProperty("Warnings")
    private List<String> warnings;
}
