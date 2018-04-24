package edu.scut.cs.hm.admin.config;

import edu.scut.cs.hm.admin.config.configurer.AppUIConfigurer;
import edu.scut.cs.hm.admin.web.interceptor.ModelAttributeInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(AppUIConfigurer.class)
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final UserDetailsService userDetailsService;

    private final AppUIConfigurer appUIConfigurer;

    @Autowired
    public WebMvcConfiguration(UserDetailsService userDetailsService, AppUIConfigurer appUIConfigurer) {
        this.userDetailsService = userDetailsService;
        this.appUIConfigurer = appUIConfigurer;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ModelAttributeInterceptor(userDetailsService, appUIConfigurer.getHeader()));
    }
}
