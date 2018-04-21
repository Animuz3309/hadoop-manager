package edu.scut.cs.hm.admin.config.configurer;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;
import java.util.Set;

@ConfigurationProperties("hm.auth")
@Data
public class PropertyUserDetailsServiceConfigurer {
    private String adminPassword = "password";  // 明文
    private Map<String, UserConfig> users;

    @Data
    public static class UserConfig {
        private String password;
        private String tenant;
        private String title;
        private String email;
        private Set<String> roles;
    }
}
