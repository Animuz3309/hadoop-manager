package edu.scut.cs.hm.model.registry;

import edu.scut.cs.hm.common.kv.mapping.KvMapping;
import edu.scut.cs.hm.model.registry.core.RegistryCredentials;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * Configuration for registry service
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PrivateRegistryConfig extends DockerRegistryConfig implements RegistryCredentials {

    @NotNull
    @KvMapping
    private String url;

    {
        setRegistryType(RegistryType.PRIVATE);
    }

    @Override
    public PrivateRegistryConfig clone() {
        return (PrivateRegistryConfig) super.clone();
    }
}
