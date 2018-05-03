package edu.scut.cs.hm.admin.security.acl;

import edu.scut.cs.hm.common.security.Authorities;
import edu.scut.cs.hm.common.security.SecurityUtils;
import edu.scut.cs.hm.common.security.acl.TenantGrantedAuthoritySid;
import edu.scut.cs.hm.common.security.acl.TenantPrincipalSid;
import edu.scut.cs.hm.common.security.acl.dto.AceSource;
import edu.scut.cs.hm.common.security.acl.dto.AclSource;
import edu.scut.cs.hm.common.security.acl.dto.ObjectIdentityData;
import edu.scut.cs.hm.common.security.acl.dto.PermissionData;

import java.io.Serializable;

public abstract class VirtualAclProvider implements AclProvider {
    private static final TenantPrincipalSid PRINCIPAL_SYS = TenantPrincipalSid.from(SecurityUtils.USER_SYSTEM);
    private static final TenantGrantedAuthoritySid GA_USER = TenantGrantedAuthoritySid.from(Authorities.USER);

    // TODO
    @Override
    public AclSource provide(Serializable id) {
        String cluster = getCluster(id);
        if (cluster == null) {
            // when node is unbound to any cluster we grant all to any user
            return makeAcl(id);
        }
        return null;
    }

    private AclSource makeAcl(Serializable id) {
        return AclSource.builder().objectIdentity(toOid(id))
                .owner(PRINCIPAL_SYS)
                .addEntry(AceSource.builder()
                        .id(GA_USER.toString())
                        .sid(GA_USER)
                        .granting(true)
                        .permission(PermissionData.ALL)
                        .build())
                .build();
    }

    protected abstract String getCluster(Serializable id);
    protected abstract ObjectIdentityData toOid(Serializable id);
}
