package edu.scut.cs.hm.docker.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PruneNetworksResponse extends ServiceCallResult {
    @JsonProperty("NetworksDeleted")
    private List<String> networks;
}
