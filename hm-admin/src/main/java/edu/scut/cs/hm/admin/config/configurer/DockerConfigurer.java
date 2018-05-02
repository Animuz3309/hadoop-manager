package edu.scut.cs.hm.admin.config.configurer;

import edu.scut.cs.hm.docker.DockerEventConfig;
import edu.scut.cs.hm.model.node.NodeServiceConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("hm.docker")
@Data
public class DockerConfigurer {
    private DockerEventConfig events;
    private NodeServiceConfig nodes;
}
