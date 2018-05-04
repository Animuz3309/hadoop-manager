package edu.scut.cs.hm.docker.model.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import edu.scut.cs.hm.docker.DockerService;
import edu.scut.cs.hm.docker.model.DockerServiceInfo;
import edu.scut.cs.hm.model.*;
import lombok.Data;

import java.beans.ConstructorProperties;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Docker service event
 */
@Data
public class DockerServiceEvent implements WithCluster, EventWithTime, WithSeverity {

    public static class DockerServiceInfoEvent extends DockerServiceEvent {
        private final DockerServiceInfo info;

        @JsonCreator
        @ConstructorProperties({"service", "node", "ngroup", "info"})
        public DockerServiceInfoEvent(String service,
                                      String node,
                                      String cluster,
                                      DockerServiceInfo info) {
            super(service, node, cluster, StandardAction.UPDATE.value());
            this.info = info;
        }

        public DockerServiceInfo getInfo() {
            return info;
        }
    }

    public static final String BUS = "bus.hm.dockerservice";
    private final LocalDateTime time = LocalDateTime.now();
    private final String service;
    private final String node;
    private final String cluster;
    private final String action;
    private final Severity severity;

    public static DockerServiceEvent onServiceInfo(DockerService service, DockerServiceInfo serviceInfo) {
        return new DockerServiceInfoEvent(service.getId(), service.getNode(), service.getCluster(), serviceInfo);
    }

    public DockerServiceEvent(DockerService service, String action) {
        this(service.getId(), service.getNode(), service.getCluster(), action);
    }

    /**
     * Do not use this constructor, it only for deserialization.
     * @param service
     * @param node
     * @param cluster
     * @param action
     */
    @JsonCreator
    @ConstructorProperties({"service", "node", "ngroup", "action"})
    public DockerServiceEvent(String service,
                              String node,
                              String cluster,
                              String action) {
        this.service = service;
        this.node = node;
        this.cluster = cluster;
        this.action = action;
        this.severity = StandardAction.toSeverity(action);
    }

    @Override
    public long eventTimeMillis() {
        return time.toEpochSecond(ZoneOffset.UTC);
    }

    @Override
    public String getCluster() {
        return cluster;
    }

}
