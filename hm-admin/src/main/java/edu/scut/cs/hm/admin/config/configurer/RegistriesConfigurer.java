package edu.scut.cs.hm.admin.config.configurer;

import edu.scut.cs.hm.model.registry.HubRegistryConfig;
import edu.scut.cs.hm.model.registry.PrivateRegistryConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties("hm.registries")
public class RegistriesConfigurer {
    private Integer connectTimeOut = 10000;
    private Integer readTimeOut = 20000;
    private String dockerSearchHubUrl = "https://registry.hub.docker.com";
    private String dockerHubUrl = "https://registry-1.docker.io";
    private long searchCacheMinutes = 10;

    /**
     * Used for synchronous initialization of registries.
     * <p/> Usually applicable for debugging and tests.
     */
    private boolean syncInit = false;

    private List<PrivateRegistryConfig> privateRegistry;
    private List<HubRegistryConfig> hubRegistry;
}
