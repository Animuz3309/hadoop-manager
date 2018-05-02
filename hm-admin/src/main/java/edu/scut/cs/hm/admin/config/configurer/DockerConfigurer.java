package edu.scut.cs.hm.admin.config.configurer;

import edu.scut.cs.hm.docker.DockerEventConfig;
import edu.scut.cs.hm.model.node.NodeServiceConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("hm.docker")
@Data
public class DockerConfigurer {
    /**
     * DockerEvent config how to accept docker event to MessageBus
     */
    private DockerEventConfig events;

    /**
     * NodeService config how to save/update/remove node
     */
    private NodeServiceConfig nodes;

    private boolean checkSsl = true;
    /**
     * rootCert.keystore to 'hm-agent' on the other physical docker node
     */
    private String agentRootCertKeystore = "/root.jks";
    /**
     * rootCert.storepass to 'hm-agent' on the other physical docker node
     */
    private String agentRootCertStorepass = "storepass";
    /**
     * password to 'hm-agent' on the other physical docker node
     */
    private String agentPassword = "password";
}
