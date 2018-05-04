package edu.scut.cs.hm.docker;

import edu.scut.cs.hm.admin.security.AccessContext;
import edu.scut.cs.hm.admin.security.AccessContextFactory;
import edu.scut.cs.hm.admin.security.SecuredType;
import edu.scut.cs.hm.common.security.acl.dto.Action;
import edu.scut.cs.hm.docker.arg.GetEventsArg;
import edu.scut.cs.hm.docker.model.network.Network;
import edu.scut.cs.hm.docker.res.ServiceCallResult;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Wrap DockerService with ACL Security
 */
public class DockerServiceSecurityWrapper implements DockerService {
    private final AccessContextFactory aclContextFactory;
    private final DockerService service;

    public DockerServiceSecurityWrapper(AccessContextFactory aclContextFactory, DockerService service) {
        this.aclContextFactory = aclContextFactory;
        this.service = service;
    }

    public void checkServiceAccess(Action action) {
        AccessContext context = aclContextFactory.getContext();
        checkServiceAccessInternal(context, action);
    }

    private void checkServiceAccessInternal(AccessContext context, Action action) {
        checkClusterAccess(context, action);
        String node = getNode();
        if(node != null) {
            boolean granted = context.isGranted(SecuredType.NODE.id(node), action);
            if(!granted) {
                throw new AccessDeniedException("Access to node docker service '" + node + "' with " + action + " is denied.");
            }
        }
    }

    private void checkClusterAccess(AccessContext context, Action action) {
        Assert.notNull(action, "Action is null");
        String cluster = getCluster();
        if(cluster != null) {
            boolean granted = context.isGranted(SecuredType.CLUSTER.id(cluster), action);
            if(!granted) {
                throw new AccessDeniedException("Access to ngroup docker service '" + cluster + "' with " + action + " is denied.");
            }
        }
    }

    @Override
    public String getCluster() {
        return service.getCluster();
    }

    @Override
    public String getNode() {
        return service.getNode();
    }

    @Override
    public String getAddress() {
        return service.getAddress();
    }

    @Override
    public boolean isOnline() {
        return service.isOnline();
    }

    @Override
    public ServiceCallResult subscribeToEvents(GetEventsArg arg) {
        checkServiceAccess(Action.READ);
        return service.subscribeToEvents(arg);
    }

    @Override
    public List<Network> getNetworks() {
        AccessContext context = aclContextFactory.getContext();
        checkServiceAccessInternal(context, Action.READ);
        return service.getNetworks().stream().filter((net) -> context.isGranted(SecuredType.NETWORK.id(net.getId()), Action.READ))
                .collect(Collectors.toList());
    }
}
