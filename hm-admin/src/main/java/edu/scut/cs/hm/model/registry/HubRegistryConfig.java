package edu.scut.cs.hm.model.registry;


import edu.scut.cs.hm.model.registry.core.RegistryCredentials;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Configuration for registry service
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HubRegistryConfig extends DockerRegistryConfig implements RegistryCredentials {

    {
        setRegistryType(RegistryType.DOCKER_HUB);
    }

    @Override
    public HubRegistryConfig clone() {
        return (HubRegistryConfig) super.clone();
    }
}
