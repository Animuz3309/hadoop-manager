package edu.scut.cs.hm.common.security;

import java.util.Objects;

/**
 * some constants and constant methods for tenancy support
 */
public final class MultiTenancySupport {

    private MultiTenancySupport() {}

    /**
     * user in cases where (String) retrieved from null or incorrect objects
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
     * if o is not the instance of {@link OwnedByTenant} return false, otherwise true
     * @param o
     * @return
     */
    public static boolean isNoTenant(Object o) {
        return Objects.equals(NO_TENANT, getTenant(o));
    }
}
