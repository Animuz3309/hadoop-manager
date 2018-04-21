package edu.scut.cs.hm.common.security;

/**
 * an domain object owned by {@link Tenant}
 */
public interface OwnedByTenant {
    /**
     * Tenant's unique identifier
     * @return
     */
    String getTenant();
}
