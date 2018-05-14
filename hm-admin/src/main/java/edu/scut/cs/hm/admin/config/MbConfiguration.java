package edu.scut.cs.hm.admin.config;

import edu.scut.cs.hm.common.mb.MessageBus;
import edu.scut.cs.hm.common.mb.MessageBuses;
import edu.scut.cs.hm.docker.model.events.DockerLogEvent;
import edu.scut.cs.hm.docker.model.events.DockerServiceEvent;
import edu.scut.cs.hm.model.application.ApplicationEvent;
import edu.scut.cs.hm.model.ngroup.NodesGroupEvent;
import edu.scut.cs.hm.model.node.NodeEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    @Bean(name = NodesGroupEvent.BUS)
    MessageBus<NodesGroupEvent> nodesGroupMessageBus() {
        return MessageBuses.create(NodesGroupEvent.BUS, NodesGroupEvent.class);
    }

    @Bean(name = ApplicationEvent.BUS)
    MessageBus<ApplicationEvent> applicationEventMessageBus() {
        return MessageBuses.create(ApplicationEvent.BUS, ApplicationEvent.class);
    }
}
