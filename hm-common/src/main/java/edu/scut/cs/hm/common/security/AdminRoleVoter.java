package edu.scut.cs.hm.common.security;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * If authentication has ADMIN_ROLE always give ACCESS_GRANTED
 */
public class AdminRoleVoter extends RoleVoter {
    @Override
    public int vote(Authentication authentication, Object object, Collection<ConfigAttribute> attributes) {
        for (GrantedAuthority authority: authentication.getAuthorities()) {
            if (authority.getAuthority().equals(Authorities.ADMIN_ROLE)) {
                return ACCESS_GRANTED;
            }
        }
        return super.vote(authentication, object, attributes);
    }
}
