package edu.scut.cs.hm.admin.config;

import edu.scut.cs.hm.admin.web.interceptor.ModelAttributeInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final UserDetailsService userDetailsService;

    @Value("${spring.application.name:Hadoop Manager Tool}")
    private String appName;

    @Autowired
    public WebMvcConfiguration(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ModelAttributeInterceptor(userDetailsService, appName));
    }
}
