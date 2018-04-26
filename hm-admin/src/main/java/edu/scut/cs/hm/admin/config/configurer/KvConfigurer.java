package edu.scut.cs.hm.admin.config.configurer;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("hm.kv")
@Data
public class KvConfigurer {

    private String[] etcdUrls;

    private String prefix;
}
