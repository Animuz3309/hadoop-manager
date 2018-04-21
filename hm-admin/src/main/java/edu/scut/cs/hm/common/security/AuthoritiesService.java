package edu.scut.cs.hm.common.security;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Authorities Service to get granted authorities
 */
public interface AuthoritiesService {
    Collection<GrantedAuthority> getAuthorities();
}
