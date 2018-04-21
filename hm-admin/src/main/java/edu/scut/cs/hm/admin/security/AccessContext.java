package edu.scut.cs.hm.admin.security;

import edu.scut.cs.hm.common.security.Authorities;
import edu.scut.cs.hm.common.security.acl.ExtPermissionGrantingStrategy;
import edu.scut.cs.hm.common.security.acl.TenantGrantedAuthoritySid;
import edu.scut.cs.hm.common.security.acl.dto.PermissionData;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.acls.model.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;

/**
 * Keep access control context, and save current authentication to invoke some permission granting strategy
 * and judge the permissions to current authentication
 * Note. current authentication get from {@link SecurityContextHolder#getContext()}, so should be care for
 * authentication the context keep is same to the {@link SecurityContextHolder#getContext()}
 */
public class AccessContext {
    private final AclService aclService;
    private final ExtPermissionGrantingStrategy pgs;
    private final List<Sid> sids;
    private final Authentication authentication;

    AccessContext(AccessContextFactory factory) {
        this.authentication = SecurityContextHolder.getContext().getAuthentication();
        List<Sid> sids;
        if (this.authentication == null) {
            throw new AccessDeniedException("No credentials in context.");
        } else {
            sids = factory.sidStrategy.getSids(authentication); // include childs sids(authority)
        }
        this.aclService = factory.aclService;
        this.pgs = factory.pgs;
        this.sids = sids;
    }

    /**
     * Check access for specified object
     * @param o
     * @param perms
     * @return
     */
    public boolean isGranted(ObjectIdentity o, Permission... perms) {
        Assert.notNull(o, "Secured object is null");
        if (isAdminFor(o)) {
            return true;
        }
        try {
            Acl acl = aclService.readAclById(o);
            return acl.isGranted(Arrays.asList(perms), sids, false);
        } catch (NotFoundException e) {
            return false;
        }
    }

    public void assertGranted(ObjectIdentity oid, Permission... perms) {
        boolean granted = isGranted(oid, perms);
        if (!granted) {
            throw new AccessDeniedException("Access to " + oid + " with " + Arrays.toString(perms) + " is denied.");
        }
    }

    private boolean isAdminFor(ObjectIdentity o) {
        final String role = Authorities.adminOf(o.getType());   // retrieve admin of this object
        for (Sid sid: sids) {
            if (!(sid instanceof TenantGrantedAuthoritySid)) {
                continue;
            }

            TenantGrantedAuthoritySid authoritySid = (TenantGrantedAuthoritySid) sid;
            String authority = authoritySid.getGrantedAuthority();

            if (Authorities.ADMIN_ROLE.equals(authority)) {
                // grant to global ADMIN
                return true;
            }
            if (role.equals(authority)) {
                // grant to admin of the object
                return true;
            }
        }
        return false;
    }

    /**
     * Get permission of the this.authentication to the specified object
     * @param oid
     * @return
     */
    public PermissionData getPermission(ObjectIdentity oid) {
        Assert.notNull(oid, "Secured object is null");
        if (isAdminFor(oid)) {
            return PermissionData.ALL;
        }
        try {
            Acl acl = aclService.readAclById(oid);
            return pgs.getPermission(acl, sids);
        } catch (NotFoundException e) {
            return PermissionData.NONE;
        }
    }

    boolean isActual() {
        return getActualAuthIfNew() == null;
    }

    void assertActual() {
        Authentication currAuth = getActualAuthIfNew();
        if (currAuth != null) {
            throw new IllegalStateException("AccessContext is for " + this.authentication
                    + " but current is " + currAuth);
        }
    }

    /**
     * compare to this.authentication and currAuth from (SecurityContextHolder.getContext().getAuthentication())
     * @return null if same
     */
    private Authentication getActualAuthIfNew() {
        SecurityContext currContext = SecurityContextHolder.getContext();
        Authentication currAuth = currContext.getAuthentication();
        // something may change this.authentication and we can not use '==', so we need to compare principal and authorities
        // it is caused by spring which can save context into http session and change its this.authentication in different thread
        if (this.authentication.getPrincipal().equals(currAuth.getPrincipal())
                && this.authentication.getAuthorities().equals(currAuth.getAuthorities())) {
            return null;
        }
        return currAuth;
    }


}
