package edu.scut.cs.hm.agent.config;

import edu.scut.cs.hm.agent.proxy.ProxyServlet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Servlet;

@Configuration
public class WebConfiguration {

    @Bean
    public Servlet dispatcherServlet() {
        return new ProxyServlet();
    }
}
