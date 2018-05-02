package edu.scut.cs.hm.admin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.scut.cs.hm.common.kv.KeyValueStorage;
import edu.scut.cs.hm.common.kv.mapping.KvMapperFactory;
import edu.scut.cs.hm.common.mb.MessageBus;
import edu.scut.cs.hm.common.mb.MessageBuses;
import edu.scut.cs.hm.docker.model.DockerLogEvent;
import edu.scut.cs.hm.docker.model.DockerServiceEvent;
import edu.scut.cs.hm.model.node.NodeEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.TextEncryptor;

@Configuration
public class MbConfiguration {

    @Bean(name = NodeEvent.BUS)
    public MessageBus<NodeEvent> nodeEventMessageBus() {
        return MessageBuses.create(NodeEvent.BUS, NodeEvent.class);
    }

    @Bean(name = DockerLogEvent.BUS)
    public MessageBus<DockerLogEvent> dockerLogEventMessageBus() {
        return MessageBuses.create(DockerLogEvent.BUS, DockerLogEvent.class);
    }

    @Bean(name = DockerServiceEvent.BUS)
    public MessageBus<DockerServiceEvent> dockerServiceEventMessageBus() {
        return MessageBuses.create(DockerServiceEvent.BUS, DockerServiceEvent.class);
    }
}
