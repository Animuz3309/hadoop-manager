package edu.scut.cs.hm.admin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.scut.cs.hm.common.utils.JacksonUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfiguration {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return JacksonUtils.objectMapperBuilder();
    }
}
