package edu.scut.cs.hm.admin.component;

import edu.scut.cs.hm.admin.security.acl.AclModifier;
import edu.scut.cs.hm.admin.security.acl.AclProvider;
import edu.scut.cs.hm.common.security.acl.dto.AclSource;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * Cluster ACL provider
 * TODO need to finish, before it we need to finish nodes group and ngroup module
 * @see edu.scut.cs.hm.admin.security.acl.ProvidersAclService
 * @see edu.scut.cs.hm.admin.config.SecurityConfiguration.AclServiceConfiguration#providers
 */
@Component("CLUSTER" /* see SecuredType*/)
public class ClusterAclProvider implements AclProvider {

    @Override
    public AclSource provide(Serializable cluster) {
        return null;
    }

    @Override
    public void update(Serializable id, AclModifier operator) {

    }

    @Override
    public void list(Consumer<AclSource> consumer) {

    }
}
