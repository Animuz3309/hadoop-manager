package edu.scut.cs.hm.model.node;

import com.google.common.net.InternetDomainName;
import edu.scut.cs.hm.admin.service.NodeService;
import edu.scut.cs.hm.common.utils.ExtendedAssert;
import edu.scut.cs.hm.docker.DockerService;
import edu.scut.cs.hm.model.container.ContainerRegistration;
import edu.scut.cs.hm.model.container.ContainerService;

/**
 * Node utils
 */
public final class NodeUtils {
    private NodeUtils() {
    }

    /**
     * Node name must be valid host name. In this method we check it.
     * @param name
     */
    public static void checkName(String name) {
        InternetDomainName.from(name);
    }

    public static DockerService getDockerByContainer(ContainerService containers, NodeService nodes, String containerId) {
        ContainerRegistration container = containers.getContainer(containerId);
        ExtendedAssert.notFound(container, "Can't find container by id " + containerId);
        return nodes.getNodeDockerService(container.getNode());
    }
}
