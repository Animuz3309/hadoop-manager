package edu.scut.cs.hm.docker;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.scut.cs.hm.docker.arg.GetEventsArg;
import edu.scut.cs.hm.docker.model.events.DockerServiceEvent;
import edu.scut.cs.hm.docker.model.DockerServiceInfo;
import edu.scut.cs.hm.docker.model.network.Network;
import edu.scut.cs.hm.docker.res.ServiceCallResult;
import edu.scut.cs.hm.model.node.NodeInfoProvider;
import lombok.Data;
import org.springframework.util.Assert;
import org.springframework.web.client.AsyncRestTemplate;

import java.util.List;
import java.util.function.Consumer;

/**
 * Docker Service implementation
 * TODO finish docker service api
 */
public class DockerServiceImpl implements DockerService {

    @Data
    public static class Builder {
        private String node;
        private String cluster;
        private DockerConfig config;
        @SuppressWarnings("deprecation")
        private AsyncRestTemplate restTemplate;
        private NodeInfoProvider nodeInfoProvider;
        private Consumer<DockerServiceEvent> eventConsumer;
        /**
         * At this interceptor you may modify building of {@link DockerServiceInfo}
         */
        private Consumer<DockerServiceInfo.Builder> infoInterceptor;
        private ObjectMapper objectMapper;

        public Builder node(String node) {
            setNode(node);
            return this;
        }

        public Builder cluster(String cluster) {
            setCluster(cluster);
            return this;
        }

        public Builder config(DockerConfig config) {
            setConfig(config);
            return this;
        }

        @SuppressWarnings("deprecation")
        public Builder restTemplate(AsyncRestTemplate restTemplate) {
            setRestTemplate(restTemplate);
            return this;
        }

        public Builder nodeInfoProvider(NodeInfoProvider nodeInfoProvider) {
            setNodeInfoProvider(nodeInfoProvider);
            return this;
        }

        public Builder eventConsumer(Consumer<DockerServiceEvent> dockerServiceBus) {
            setEventConsumer(dockerServiceBus);
            return this;
        }

        public Builder infoInterceptor(Consumer<DockerServiceInfo.Builder> infoInterceptor) {
            setInfoInterceptor(infoInterceptor);
            return this;
        }

        public Builder objectMapper(ObjectMapper objectMapper) {
            setObjectMapper(objectMapper);
            return this;
        }

        public DockerServiceImpl build() {
            return new DockerServiceImpl(this);
        }
    }

    private final String node;
    private final String cluster;
    private final DockerConfig config;
    @SuppressWarnings("deprecation")
    private final AsyncRestTemplate restTemplate;
    private final NodeInfoProvider nodeInfoProvider;
    private final Consumer<DockerServiceEvent> eventConsumer;
    private final Consumer<DockerServiceInfo.Builder> infoInterceptor;
    private final ObjectMapper objectMapper;

    private final String id;

    public static Builder builder() {
        return new Builder();
    }

    public DockerServiceImpl(Builder b) {
        //========================= from builder =================================
        this.node = b.node;
        this.cluster = b.cluster;
        Assert.isTrue((this.node == null || this.cluster == null) && this.node != this.cluster,
                "Invalid config of service: cluster=" + this.cluster + " node=" + node + " service must has only one non null value.");
        this.config = b.config.validate();
        this.restTemplate = b.restTemplate;
        Assert.notNull(this.restTemplate, "restTemplate is null");
        this.nodeInfoProvider = b.nodeInfoProvider;
        Assert.notNull(this.nodeInfoProvider, "nodeInfoProvider is null");
        this.eventConsumer = b.eventConsumer;
        Assert.notNull(this.eventConsumer, "eventConsumer is null");
        this.infoInterceptor = b.infoInterceptor;
        this.objectMapper = b.objectMapper;
        Assert.notNull(this.objectMapper, "objectMapper is null");

        this.id = DockerService.super.getId();  //cache id

    }

    @Override
    public String getCluster() {
        return null;
    }

    @Override
    public String getNode() {
        return null;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getAddress() {
        return null;
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public ServiceCallResult subscribeToEvents(GetEventsArg arg) {
        return null;
    }

    @Override
    public List<Network> getNetworks() {
        return null;
    }
}
