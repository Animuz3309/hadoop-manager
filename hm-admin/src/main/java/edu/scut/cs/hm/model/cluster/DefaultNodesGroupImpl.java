package edu.scut.cs.hm.model.cluster;

import edu.scut.cs.hm.admin.service.NodeService;
import edu.scut.cs.hm.docker.DockerService;
import edu.scut.cs.hm.docker.arg.GetContainersArg;
import edu.scut.cs.hm.docker.model.container.DockerContainer;
import edu.scut.cs.hm.model.node.Node;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Node group managed 'manually', not like cluster united by docker in swarm mode or swarm.
 * It allow to view multiple nodes as single entity.
 */
@ToString(callSuper = true)
public class DefaultNodesGroupImpl extends AbstractNodesGroup<DefaultNodesGroupConfig> {

    public interface ContainersProvider {
        List<DockerContainer> getContainers(DefaultNodesGroupImpl ng, GetContainersArg arg);
    }

    private static class DefaultContainersProvider implements ContainersProvider {

        @Override
        public List<DockerContainer> getContainers(DefaultNodesGroupImpl ng, GetContainersArg arg) {
            List<DockerContainer> list = new ArrayList<>();
            NodeService nodeService = ng.getNodeService();
            for (Node node: ng.getNodes()) {
                DockerService service = nodeService.getNodeDockerService(node.getName());
            }
        }
    }
}
