package edu.scut.cs.hm.admin.config;

import edu.scut.cs.hm.admin.web.tty.WsTtyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfiguration implements WebSocketConfigurer {

    @Autowired
    private WsTtyHandler wsTtyHandler;

    @Bean
    WebSocketClient webSocketClient() {
        return new StandardWebSocketClient();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(wsTtyHandler, "/ws/tty")
                .setAllowedOrigins("*")
                .addInterceptors()
                .withSockJS();
    }
}
