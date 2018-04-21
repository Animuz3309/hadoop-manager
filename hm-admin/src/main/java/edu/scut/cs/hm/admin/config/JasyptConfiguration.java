package edu.scut.cs.hm.admin.config;

import com.ulisesbocchio.jasyptspringboot.annotation.EncryptablePropertySource;
import org.springframework.context.annotation.Configuration;

/**
 * Jasypt Configuration
 * @see com.ulisesbocchio.jasyptspringboot.configuration.EncryptablePropertyResolverConfiguration
 */
@Configuration
@EncryptablePropertySource("classpath:encrypted.properties")
public class JasyptConfiguration {
}
