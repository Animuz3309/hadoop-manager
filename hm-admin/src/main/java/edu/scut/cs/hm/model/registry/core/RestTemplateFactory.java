package edu.scut.cs.hm.model.registry.core;

import edu.scut.cs.hm.model.registry.core.RegistryAuthAdapter;
import org.springframework.web.client.RestTemplate;

/**
 * Create {@link RestTemplate} for get/post to registry
 */
public interface RestTemplateFactory {
    RestTemplate create(RegistryAuthAdapter raa);
}