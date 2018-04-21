package edu.scut.cs.hm.common.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * List of default granted authorities <p/>
 * and static methods to construct granted authority by name and tenant
 */
public final class Authorities {

    private Authorities() {
    }

    public static final String ADMIN_ROLE = "ROLE_ADMIN";
    public static final String USER_ROLE = "ROLE_USER";

    public static final TenantGrantedAuthority ADMIN = fromName(ADMIN_ROLE);
    public static final TenantGrantedAuthority USER = fromName(USER_ROLE);

    /**
     * Make authority from its name with tenant {@link MultiTenancySupport#NO_TENANT}
     * @param name starts with "ROLE_" if not will automatically add to
     * @return customer GrantedAuthority {@link GrantedAuthorityImpl}
     */
    public static TenantGrantedAuthority fromName(String name) {
        return fromName(name, MultiTenancySupport.NO_TENANT);
    }

    /**
     * Make authority from its name and tenant
     * @param name starts with "ROLE_" if not will automatically add to
     * @return customer GrantedAuthority {@link GrantedAuthorityImpl}
     */
    public static TenantGrantedAuthority fromName(String name, String tenant) {
        name = name.toUpperCase();
        if (!name.startsWith("ROLE_")) {
            name = "ROLE_" + name;
        }
        return new GrantedAuthorityImpl(name, tenant);
    }

    /**
     * Apply specific function to check each authority of all authorities retrieved from userDetails. <p/>
     * when a function return 'true' then loop will be broken and return 'true'
     * @param userDetails
     * @param authorityChecker
     * @return
     */
    public static boolean checkAuthorities(UserDetails userDetails,
                                           Function<GrantedAuthority, Boolean> authorityChecker) {
        if (userDetails == null) {
            return false;
        }

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        if (authorities == null) {
            return false;
        }

        for (GrantedAuthority authority: authorities) {
            Boolean res = authorityChecker.apply(authority);
            if (res != null && res) {
                return true;
            }
        }

        return false;
    }

    /**
     * Return 'true' if user has any of specified authorities
     * @param userDetails user details
     * @param authoritiesNames specified authorities
     * @return
     */
    public static boolean hasAnyOfAuthorities(UserDetails userDetails, String... authoritiesNames) {
        Set<String> set = new HashSet<>(Arrays.asList(authoritiesNames));
        return checkAuthorities(userDetails, new AnyAuthorityChecker(set));
    }

    /**
     * Return name of admin authority of specified type
     * @param type specified operation of something about authority
     * @return "ROLE_" + type.toUpperCase() + "_ADMIN"
     */
    public static String adminOf(String type) {
        return "ROLE_" + type.toUpperCase() + "_ADMIN";
    }

    /**
     * Return name of user authority of specified type
     * @param type specified operation of something about authority
     * @return "ROLE_" + type.toUpperCase() + "_USER"
     */
    public static String userOf(String type) {
        return "ROLE_" + type.toLowerCase() + "_USER";
    }

    private static class AnyAuthorityChecker implements Function<GrantedAuthority, Boolean> {
        private final Set<String> set;

        AnyAuthorityChecker(Set<String> set) {
            this.set = set;
        }

        @Override
        public Boolean apply(GrantedAuthority authority) {
            return set.contains(authority.getAuthority());
        }
    }
}
