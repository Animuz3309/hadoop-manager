package edu.scut.cs.hm.admin.config;

import edu.scut.cs.hm.admin.config.configurer.DockerConfigurer;
import edu.scut.cs.hm.admin.config.configurer.KvConfigurer;
import edu.scut.cs.hm.admin.config.configurer.SwarmProcessesConfigurer;
import edu.scut.cs.hm.docker.model.events.DockerEventConfig;
import edu.scut.cs.hm.model.cluster.SwarmDiscoveryUrlFunction;
import edu.scut.cs.hm.model.node.NodeStorageConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.stream.Collectors;

@Configuration
@EnableConfigurationProperties({DockerConfigurer.class, KvConfigurer.class, SwarmProcessesConfigurer.class})
public class DockerConfiguration {

    @Bean
    public DockerEventConfig dockerEventConfig(DockerConfigurer configurer) {
        return configurer.getEvent();
    }

    @Bean
    public NodeStorageConfig nodeStorageConfig(DockerConfigurer configurer) {
        return configurer.getNode();
    }

    @Bean
    public DockerConfigurer.AgentConfig agentConfig(DockerConfigurer configurer) {
        return configurer.getAgent();
    }

    @Bean
    public DockerConfigurer.DockerServicesConfig dockerServicesConfig(DockerConfigurer configurer) {
        return configurer.getServices();
    }

    @Bean
    public SwarmDiscoveryUrlFunction swarmDiscoveryUrlFunction(KvConfigurer kvConfigurer) {
        return new SwarmDiscoveryUrlFunction.Etcd(Arrays.stream(kvConfigurer.getEtcdUrls())
                .map((s) -> s.substring(s.lastIndexOf('/') + 1))
                .collect(Collectors.toList()));
    }
}
