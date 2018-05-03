package edu.scut.cs.hm.docker.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CreateNetworkResponse extends ServiceCallResult {

    @JsonProperty("Id")
    private String id;

    @JsonProperty("Warning")
    private String warning;
}
