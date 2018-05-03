package edu.scut.cs.hm.docker.res;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CreateContainerResponse extends ServiceCallResult {

    @JsonProperty("Id")
    private String id;

    @JsonProperty("Warnings")
    private List<String> warnings;

}
