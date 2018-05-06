package edu.scut.cs.hm.model.registry;

import org.springframework.web.client.RestTemplate;

public interface RestTemplateFactory {
    RestTemplate create(RegistryAuthAdapter raa);
}