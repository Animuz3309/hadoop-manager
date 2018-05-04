package edu.scut.cs.hm.admin.config.configurer;

import edu.scut.cs.hm.docker.model.events.DockerEventConfig;
import edu.scut.cs.hm.model.node.NodeStorageConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("hm.docker")
@Data
public class DockerConfigurer {
    /**
     * DockerEvent config how to accept docker event to MessageBus
     */
    private DockerEventConfig event = new DockerEventConfig();

    /**
     * NodeService config how to save/update/remove node
     */
    private NodeStorageConfig node = new NodeStorageConfig();

    /**
     * Docker agent config which installed in other docker physical node
     */
    private AgentConfig agent = new AgentConfig();

    @Data
    public static class AgentConfig {
        private boolean checkSsl = true;
        /**
         * rootCert.keystore to 'hm-agent' on the other physical docker node
         */
        private String rootCertKeystore = "/root.jks";
        /**
         * rootCert.storepass to 'hm-agent' on the other physical docker node
         */
        private String rootCertStorepass = "storepass";
        /**
         * password to 'hm-agent' on the other physical docker node
         */
        private String password = "password";
    }
}
