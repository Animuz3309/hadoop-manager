package edu.scut.cs.hm.docker.arg;

import edu.scut.cs.hm.docker.model.auth.AuthConfig;
import edu.scut.cs.hm.docker.model.swarm.Service;
import lombok.Data;

/**
 * 'Create a service. When using this endpoint to create a service using a private repository from the registry,
 * the X-Registry-Auth header must be used to include a base64-encoded AuthConfig object. Refer to the create
 * an image section for more details.'
 */
@Data
public class UpdateServiceArg {
    private AuthConfig registryAuth;
    /**
     * id or name
     */
    private String service;
    /**
     * The version number of the service object being updated. This is required to avoid conflicting writes.
     */
    private long version;
    private Service.ServiceSpec spec;
}
