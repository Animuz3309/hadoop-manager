package edu.scut.cs.hm.admin.component;

import edu.scut.cs.hm.admin.security.SecuredType;
import edu.scut.cs.hm.admin.security.acl.VirtualAclProvider;
import edu.scut.cs.hm.admin.service.NodeStorage;
import edu.scut.cs.hm.common.security.acl.dto.ObjectIdentityData;
import edu.scut.cs.hm.model.container.ContainerRegistration;
import edu.scut.cs.hm.model.container.ContainerStorage;
import edu.scut.cs.hm.model.container.ContainerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component("CONTAINER" /*see secured type*/)
@Lazy
public class ContainersAclProvider extends VirtualAclProvider {

    private final ContainerStorage containers;
    private final NodeStorage nodes;

    @Autowired
    public ContainersAclProvider(ClusterAclProvider clusterAclProvider, ContainerStorage containers, NodeStorage nodes) {
        super(clusterAclProvider);
        this.containers = containers;
        this.nodes = nodes;
    }

    @Override
    protected String getCluster(Serializable id) {
        String strId = (String) id;
        if(!ContainerUtils.isContainerId(strId)) {
            throw new IllegalArgumentException("Invalid container id: " + id);
        }
        ContainerRegistration cr = containers.getContainer(strId);
        if(cr == null) {
            throw new NotFoundException("Container '" + id + "' is not registered.");
        }
        String node = cr.getNode();
        return nodes.getNodeCluster(node);
    }

    @Override
    protected ObjectIdentityData toOid(Serializable id) {
        return SecuredType.CONTAINER.id((String) id);
    }
}

