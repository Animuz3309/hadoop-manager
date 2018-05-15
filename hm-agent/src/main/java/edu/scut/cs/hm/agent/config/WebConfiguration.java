package edu.scut.cs.hm.agent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@Configuration
@EnableWebSocket
public class WebConfiguration {

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }


}
