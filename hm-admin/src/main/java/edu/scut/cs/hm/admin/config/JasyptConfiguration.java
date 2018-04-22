package edu.scut.cs.hm.admin.config;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Jasypt Configuration
 * 不要让敏感信息以明文方式在配置文件中存在
 * @see com.ulisesbocchio.jasyptspringboot.configuration.EncryptablePropertyResolverConfiguration
 */
@Configuration
// 可以不加,spring-boot 自动配置
@EnableEncryptableProperties
@PropertySource("classpath:encrypted.properties")
public class JasyptConfiguration {
}
