package edu.scut.cs.hm.admin.config;

import edu.scut.cs.hm.admin.config.configurer.DockerConfigurer;
import edu.scut.cs.hm.docker.model.events.DockerEventConfig;
import edu.scut.cs.hm.model.node.NodeStorageConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DockerConfigurer.class)
public class DockerConfiguration {

    private final DockerConfigurer configurer;

    @Autowired
    public DockerConfiguration(DockerConfigurer configurer) {
        this.configurer = configurer;
    }

    @Bean
    public DockerEventConfig dockerEventConfig() {
        return configurer.getEvent();
    }

    @Bean
    public NodeStorageConfig nodeServiceConfig() {
        return configurer.getNode();
    }

    @Bean
    public DockerConfigurer.AgentConfig agentConfig() {
        return configurer.getAgent();
    }
}
