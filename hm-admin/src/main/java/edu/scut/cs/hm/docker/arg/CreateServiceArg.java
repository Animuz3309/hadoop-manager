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
public class CreateServiceArg {
    private AuthConfig registryAuth;
    private Service.ServiceSpec spec;
}
