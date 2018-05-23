package edu.scut.cs.hm.common.security;

import edu.scut.cs.hm.common.security.acl.TenantPrincipalSid;
import edu.scut.cs.hm.common.security.acl.TenantSid;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

/**
 * some constants and constant methods for tenancy support
 */
public final class MultiTenancySupport {

    private MultiTenancySupport() {}

    /**
     * username in cases where (String) retrieved from null or incorrect objects
     */
    public static final String NO_TENANT = null;
    public static final String ANONYMOUS_TENANT = "anonymous_tenant";
    public static final String ROOT_TENANT = "root";

    /**
     * Retrieved tenant(String) from object if it is instance of {@link OwnedByTenant},
     * otherwise return {@link #NO_TENANT}
     * @param object
     * @return
     */
    public static String getTenant(Object object) {
        if (object instanceof OwnedByTenant) {
            return ((OwnedByTenant) object).getTenant();
        }
        return NO_TENANT;
    }

    /**
     * Fix null tenant for principals and validate.
     * @param sid
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends TenantSid> T fixTenant(T sid) {
        if(sid == null) {
            return sid;
        }
        final String tenant = sid.getTenant();
        if(sid instanceof GrantedAuthoritySid && tenant == null) {
            return sid;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        ExtendedUserDetails eud = (ExtendedUserDetails) auth.getPrincipal();
        final String authTenant = eud.getTenant();
        if(authTenant.equals(tenant)) {
            return sid;
        }
        if(tenant == null) {
            return (T) TenantPrincipalSid.from((PrincipalSid) sid);
        }
        if(!ROOT_TENANT.equals(authTenant)) {
            // we must check tenancy access through TenantHierarchy, but now we does not have any full tenancy support
            throw new IllegalArgumentException("Sid " + sid + " has incorrect tenant: " + tenant + " it allow only for root tenant.");
        }
        return sid;
    }

    /**
     * if o is not the instance of {@link OwnedByTenant} return false, otherwise true
     * @param o
     * @return
     */
    public static boolean isNoTenant(Object o) {
        return Objects.equals(NO_TENANT, getTenant(o));
    }
}
