package edu.scut.cs.hm.model.registry.core;

import edu.scut.cs.hm.model.registry.RegistryConfig;
import org.springframework.web.client.RestTemplate;

public interface RegistryAdapter {
    RestTemplate getRestTemplate();
    String getUrl();
    RegistryConfig getConfig();

    RegistryCredentials getCredentials();

    /**
     * Handle name before it will be passed into url.
     * @param name
     * @return name or it variation, never null
     */
    default String adaptNameForUrl(String name) {
        return name;
    }
}