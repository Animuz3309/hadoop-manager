package edu.scut.cs.hm.admin.config;

import edu.scut.cs.hm.common.mb.MessageBus;
import edu.scut.cs.hm.common.mb.MessageBuses;
import edu.scut.cs.hm.docker.model.DockerLogEvent;
import edu.scut.cs.hm.model.node.NodeEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MbConfiguration {

    @Bean(name = NodeEvent.BUS)
    public MessageBus<NodeEvent> nodeMessageBus() {
        return MessageBuses.create(NodeEvent.BUS, NodeEvent.class);
    }

    @Bean(name = DockerLogEvent.BUS)
    public MessageBus<DockerLogEvent> dockerMessageBus() {
        return MessageBuses.create(DockerLogEvent.BUS, DockerLogEvent.class);
    }
}
