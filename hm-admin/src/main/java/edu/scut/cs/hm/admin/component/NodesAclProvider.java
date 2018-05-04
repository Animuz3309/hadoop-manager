package edu.scut.cs.hm.admin.component;

import edu.scut.cs.hm.admin.security.SecuredType;
import edu.scut.cs.hm.admin.security.TempAuth;
import edu.scut.cs.hm.admin.security.acl.VirtualAclProvider;
import edu.scut.cs.hm.admin.service.NodeStorage;
import edu.scut.cs.hm.common.security.acl.dto.ObjectIdentityData;
import edu.scut.cs.hm.model.NotFoundException;
import edu.scut.cs.hm.model.node.NodeRegistration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @see edu.scut.cs.hm.admin.security.acl.ProvidersAclService
 * @see edu.scut.cs.hm.admin.config.SecurityConfiguration.AclServiceConfiguration#providers
 */
@Component("NODE" /* see SecuredType */)
public class NodesAclProvider extends VirtualAclProvider {
    private NodeStorage nodeStorage;

    @Autowired
    public NodesAclProvider(ClusterAclProvider clusterAclProvider, NodeStorage nodeStorage) {
        super(clusterAclProvider);
        this.nodeStorage = nodeStorage;
    }

    @Override
    protected String getCluster(Serializable id) {
        String nodeId = String.valueOf(id);
        NodeRegistration nr;
        try (TempAuth auth = TempAuth.asSystem()) {
            nr = nodeStorage.getNodeRegistration(nodeId);
        }
        if (nr == null) {
            throw new NotFoundException("Node '" + id +"' is not registered");
        }
        return nr.getCluster();
    }

    @Override
    protected ObjectIdentityData toOid(Serializable id) {
        return SecuredType.NODE.id((String) id);
    }
}
