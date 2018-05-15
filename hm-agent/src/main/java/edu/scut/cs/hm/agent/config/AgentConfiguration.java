package edu.scut.cs.hm.agent.config;

import edu.scut.cs.hm.agent.config.props.NotifierProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@Configuration
@EnableConfigurationProperties(NotifierProperties.class)
@EnableWebSocket
public class AgentConfiguration {
    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
