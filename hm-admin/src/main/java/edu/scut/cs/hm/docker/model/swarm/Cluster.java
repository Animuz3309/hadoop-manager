package edu.scut.cs.hm.docker.model.swarm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Base class for cluster info in 'GET /swarm' (this use subclass with tokens) and 'GET /info' requests. <p/>
 * <pre>
 {
 "ID":"6r...lt",
 "Version":{"Index":11},
 "CreatedAt":"2016-12-29T15:26:15.372810703Z",
 "UpdatedAt":"2016-12-29T15:26:15.474602597Z",
 "Spec": // see {@link SwarmSpec}
 }
 * </pre>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class Cluster extends MetaMutable {
    @JsonProperty("Spec")
    private SwarmSpec spec;

}
