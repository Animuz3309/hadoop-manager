package edu.scut.cs.hm.common.security.acl;

import edu.scut.cs.hm.common.security.MultiTenancySupport;
import edu.scut.cs.hm.common.security.OwnedByTenant;
import edu.scut.cs.hm.common.security.TenantService;
import edu.scut.cs.hm.common.security.acl.dto.PermissionData;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;

/**
 * The strategy which implement permission granted mechanism with considering
 * of tenant user attribute <p/>
 */
public final class TenantBasedPermissionGrantedStrategy implements ExtPermissionGrantingStrategy {

    private final PermissionGrantingJudge defaultBehavior;

    private UserDetailsService userDetailsService;
    TenantService<?> tenantService;

    public TenantBasedPermissionGrantedStrategy(PermissionGrantingJudge defaultBehavior,
                                                UserDetailsService userDetailsService,
                                                TenantService<?> tenantService) {
        this.defaultBehavior = defaultBehavior;
        this.userDetailsService = userDetailsService;
        this.tenantService = tenantService;
    }

    /**
     * Judge the request permission to 'acl' is grant to the 'sids'
     * @param acl
     * @param requests
     * @param sids
     * @param administrativeMode
     * @return
     */
    @Override
    public boolean isGranted(Acl acl, List<Permission> requests, List<Sid> sids, boolean administrativeMode) {
        PermissionData granted = getPermission(acl, sids);  // 当前访问者sids访问acl控制的对象是拥有的permission
        final int grantedMask = granted.getMask();
        boolean allow = false;
        for (Permission request: requests) {
            int reqMask = request.getMask();
            if ((reqMask & grantedMask) == reqMask) {
                allow = true;
            }
            if (!allow) {
                // each false is mean disallow
                break;
            }
        }
        return allow;
    }

    /**
     * Return the PermissionData granted to a particular sid or sids by an {@link Acl}
     * @param acl acl of a entry to access
     * @param sids particular sid or sids
     * @return
     */
    @Override
    public PermissionData getPermission(Acl acl, List<Sid> sids) {
        Assert.notNull(tenantService, "tenantService is null");
        Assert.notNull(userDetailsService, "userDetailsService is null");

        final Sid ownerSid = acl.getOwner();
        final String ownerSidTenant = getTenantFromSid(ownerSid);
        if (Objects.equals(ownerSidTenant, MultiTenancySupport.NO_TENANT)) {
            throw new RuntimeException("Can not retrieve tenant from acl owner: acl.objectIdentity=" +
                    acl.getObjectIdentity().getIdentifier());
        }
        final String currentPrincipalTenant = getPrincipalSidTenant(sids);

        PermissionGrantingContext pgc = new PermissionGrantingContext(this, ownerSid, currentPrincipalTenant);
        final List<AccessControlEntry> aces = acl.getEntries();     // aces belong to entry's (want to access) acl
        pgc.setHasAces(!aces.isEmpty());

        PermissionData.Builder pb = PermissionData.builder();
        pb.add(defaultBehavior.getPermission(pgc));

        for (int aceIndex = 0; aceIndex < aces.size(); aceIndex++) {
            // ACE represents an individual permission assignment within an Acl.
            final AccessControlEntry ace = aces.get(aceIndex);
            final Sid aceSid = ace.getSid();
            final String aceTenant = getTenantFromSid(aceSid);
            for (int sidIndex = 0; sidIndex < sids.size(); sidIndex++) {
                final Sid sid = sids.get(sidIndex);
                pgc.setCurrentSid(sid);

                // 只有当前访问者某个sid的tenant与ace的tenant相等才能继续，因为权限受tenant控制
                if (aceTenant != null && !pgc.getCurrentTenants().contains(aceTenant)) {
                    continue;
                }
                // 比较ace的sid与当前访问者的某个sid
                if (!compareSids(sid, aceSid)) {
                    continue;
                }
                // Indicates the permission is being granted to the relevant Sid
                Permission acep = ace.getPermission();
                if (ace.isGranting()) {
                    pb.add(acep);
                } else {
                    pb.remove(acep);
                }
            }
        }
        return pb.build();
    }

    private boolean compareSids(Sid authSid, Sid aceSid) {
        if (MultiTenancySupport.isNoTenant(aceSid)) {
            // ace sid can has not tenant, we must consider this
            if (authSid instanceof GrantedAuthoritySid) {
                return (aceSid instanceof GrantedAuthoritySid) && Objects.equals(
                        ((GrantedAuthoritySid) authSid).getGrantedAuthority(),
                        ((GrantedAuthoritySid) aceSid).getGrantedAuthority()
                );
            }

            if (authSid instanceof PrincipalSid) {
                return (aceSid instanceof PrincipalSid) && Objects.equals(
                        ((PrincipalSid) authSid).getPrincipal(),
                        ((PrincipalSid) aceSid).getPrincipal()
                );
            }
        }
        // there a unsupported sids or its has tenant, compare its as usual objects
        return aceSid.equals(authSid);
    }

    private String getPrincipalSidTenant(List<Sid> sids) throws IllegalArgumentException {
        PrincipalSid principalSid = null;
        for (final Sid sid: sids) {
            if (sid instanceof PrincipalSid) {
                if (principalSid != null && !principalSid.equals(sid)) {
                    throw new IllegalArgumentException("We found more than one PrincipalSid: " +
                            principalSid +
                            " and" +
                            sid);
                }
                principalSid = (PrincipalSid) sid;
            }
        }
        if (principalSid == null) {
            throw new IllegalArgumentException("We can't find PrincipalSid");
        }
        String tenant = getTenantFromSid(principalSid);
        if (Objects.equals(tenant, MultiTenancySupport.NO_TENANT)) {
            throw new IllegalArgumentException("No 'tenant' found in PrincipalSid");
        }
        return tenant;
    }

    String getTenantFromSid(final Sid sid) {
        String tenant = MultiTenancySupport.getTenant(sid);
        if (tenant == null && sid instanceof PrincipalSid) {
            final PrincipalSid owner = (PrincipalSid) sid;
            final OwnedByTenant user = (OwnedByTenant) userDetailsService.loadUserByUsername(owner.getPrincipal());
            tenant = user.getTenant();
        }
        return tenant;
    }
}
