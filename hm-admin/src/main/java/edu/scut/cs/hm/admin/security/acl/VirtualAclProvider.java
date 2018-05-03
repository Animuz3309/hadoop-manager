package edu.scut.cs.hm.admin.security.acl;

import edu.scut.cs.hm.admin.component.ClusterAclProvider;
import edu.scut.cs.hm.common.security.Authorities;
import edu.scut.cs.hm.common.security.SecurityUtils;
import edu.scut.cs.hm.common.security.acl.TenantGrantedAuthoritySid;
import edu.scut.cs.hm.common.security.acl.TenantPrincipalSid;
import edu.scut.cs.hm.common.security.acl.dto.*;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * AclProvider that provide AclSource(Acl) from object that has support with cluster
 */
public abstract class VirtualAclProvider implements AclProvider {
    private static final TenantPrincipalSid PRINCIPAL_SYS = TenantPrincipalSid.from(SecurityUtils.USER_SYSTEM);
    private static final TenantGrantedAuthoritySid GA_USER = TenantGrantedAuthoritySid.from(Authorities.USER);

    private ClusterAclProvider clusterAclProvider;

    public VirtualAclProvider(ClusterAclProvider clusterAclProvider) {
        this.clusterAclProvider = clusterAclProvider;
    }

    /**
     * Get AclSource(Acl) by id (maybe node's name, container's id or other identify)
     * @param id
     * @return
     */
    @Override
    public AclSource provide(Serializable id) {
        String cluster = getCluster(id);
        if (cluster == null) {
            // when node is unbound to any cluster we grant all to any user
            return makeAcl(id);
        }
        AclSource clusterAcl = clusterAclProvider.provide(cluster);
        AclSource.Builder aclsb = AclSource.builder()
                .objectIdentity(toOid(id))
                .owner(clusterAcl.getOwner());
        clusterAcl.getEntries().forEach(ace -> {
            boolean read = ace.getPermission().has(Action.READ);
            boolean alterInside = ace.getPermission().has(Action.ALTER_INSIDE);
            if (!read && !alterInside) {
                return;
            }
            // judge this ace's permission is or not granting
            boolean granting = ace.isGranting();
            PermissionData.Builder pdb = PermissionData.builder();

            if (read) {
                pdb.add(Action.READ);
            }
            if (alterInside) {
                // cluster 'alter' mean that user can do anything with underline objects
                pdb.add(PermissionData.ALL);
                if (!read && !granting) {
                    // if read is not specified before and it is 'revoke' entry(granting = false), then we must remove READ
                    pdb.remove(Action.READ);
                }
            }
            aclsb.addEntry(AceSource.builder()
                    .id(ace.getId())
                    .sid(ace.getSid())
                    .granting(granting)
                    .permission(pdb)
                    .build());
        });
        return aclsb.build();
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

    @Override
    public void update(Serializable id, AclModifier operator) {
        // not support yet
    }

    @Override
    public void list(Consumer<AclSource> consumer) {
        // not support yet
    }
}
