package edu.scut.cs.hm.common.security;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.GrantedAuthority;

/**
 * Customer extends Spring Security <code>GrantedAuthority</code> and <code>ConfigAttribute</code>
 * with support of tenant {@link Tenant}
 * @see org.springframework.security.core.GrantedAuthority
 * @see org.springframework.security.access.ConfigAttribute
 */
public interface TenantGrantedAuthority extends GrantedAuthority, OwnedByTenant, ConfigAttribute {
}
