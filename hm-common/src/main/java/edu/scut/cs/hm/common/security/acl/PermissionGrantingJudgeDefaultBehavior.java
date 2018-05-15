package edu.scut.cs.hm.common.security.acl;

import edu.scut.cs.hm.common.security.Authorities;
import edu.scut.cs.hm.common.security.MultiTenancySupport;
import edu.scut.cs.hm.common.security.TenantService;
import edu.scut.cs.hm.common.security.acl.dto.PermissionData;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.model.Sid;

import java.util.Objects;

/**
 * Implementation of {@link edu.scut.cs.hm.common.security.acl.PermissionGrantingJudge}
 * which provide defaultBehavior for strategy
 */
public class PermissionGrantingJudgeDefaultBehavior implements PermissionGrantingJudge {

    private final TenantService<?> tenantService;

    public PermissionGrantingJudgeDefaultBehavior(TenantService<?> tenantService) {
        this.tenantService = tenantService;
    }

    /**
     * admin or root authority permission judge
     * @param context
     * @return
     */
    @Override
    public PermissionData getPermission(PermissionGrantingContext context) {
        PermissionData.Builder pdb = PermissionData.builder();
        final Sid currSid = context.getCurrentSid();
        // by ADMIN authority, sid is GrantedAuthoritySid
        if (currSid instanceof GrantedAuthoritySid
                && Authorities.ADMIN_ROLE.equals(((GrantedAuthoritySid) currSid).getGrantedAuthority())) {
            // currSid is Authorities.ADMIN_ROLE than judge by tenant
            final String tenant = MultiTenancySupport.getTenant(currSid);
            // if role.tenant == (ROOT or owner.tenant) or role's children tenant contains owner.tenant
            if (isRootTenant(tenant)
                    || (!Objects.equals(tenant, MultiTenancySupport.NO_TENANT) ?
                    tenant.equals(context.getOwnerTenant()) :
                    context.getCurrentTenants().contains(context.getOwnerTenant()))) {
                pdb.add(PermissionData.ALL);
            }
        }
        // sid is other sid like PrincipalSid
        if (PermissionData.ALL.getMask() != pdb.getMask()) {
            if (isAllowByOwner(context)) {
                pdb.add(PermissionData.ALL);
            }
        }
        return pdb.build();
    }

    private boolean isAllowByOwner(PermissionGrantingContext context) {
        // if is owner then allow all permissions
        return context.getOwnerSid().equals(context.getCurrentSid());
    }

    private boolean isRootTenant(String tenant) {
        return tenantService.isRoot(tenant);
    }
}
