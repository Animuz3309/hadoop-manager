package edu.scut.cs.hm.model.registry;

import edu.scut.cs.hm.common.kv.mapping.KvMapping;
import edu.scut.cs.hm.common.kv.mapping.PropertyCipher;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class DockerRegistryConfig extends RegistryConfig implements RegistryCredentials {

    @KvMapping
    private String username;
    @KvMapping(interceptors = PropertyCipher.class)
    private String password;

    @Override
    public void cleanCredentials() {
        setPassword(null);
    }
}