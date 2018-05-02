package edu.scut.cs.hm.admin.service;

import edu.scut.cs.hm.docker.DockerService;
import edu.scut.cs.hm.docker.DockerEventConfig;
import edu.scut.cs.hm.docker.model.DockerLogEvent;
import edu.scut.cs.hm.model.node.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Data
@Service
public class NodeService implements NodeInfoProvider, NodeRegistry {
    private final DockerEventConfig dockerEventConfig;
    private final NodeServiceConfig config;

    /**
     * create docker service of swarmNode
     * @param nr
     * @return
     */
    public DockerService createNodeDockerService(NodeRegistrationImpl nr) {
        return null;
    }

    public void acceptDockerLogEvent(DockerLogEvent e) {

    }

    public boolean fireNodePreModification(NodeInfoImpl old, NodeInfoImpl curr) {
        return false;
    }

    public void fireNodeModification(NodeRegistrationImpl nr, NodeEvent.Action action, NodeInfoImpl old, NodeInfoImpl current) {

    }
}
