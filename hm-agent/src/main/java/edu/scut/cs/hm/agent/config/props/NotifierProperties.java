package edu.scut.cs.hm.agent.config.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("hm.agent.notifier")
public class NotifierProperties {
    private String secret;
    private String rootPath;
    private String server;
    private String address;
    private long initialDelay = 60L;
    private long period = 60L;
}
