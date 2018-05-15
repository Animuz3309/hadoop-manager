package edu.scut.cs.hm.common.security.acl;

import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.acls.model.SidRetrievalStrategy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implementation of {@link SidRetrievalStrategy} which produce tenant-owned SIDs
 */
public class TenantSidRetrievalStrategy implements SidRetrievalStrategy {

    private RoleHierarchy roleHierarchy;

    public TenantSidRetrievalStrategy(RoleHierarchy roleHierarchy) {
        Assert.notNull(roleHierarchy, "RoleHierarchyImpl must be not null");
        this.roleHierarchy = roleHierarchy;
    }

    @Override
    public List<Sid> getSids(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities =
                roleHierarchy.getReachableGrantedAuthorities(authentication.getAuthorities());
        List<Sid> sids = new ArrayList<>(authorities.size() + 1);

        sids.add(new TenantPrincipalSid(authentication));

        for (GrantedAuthority authority: authorities) {
            sids.add(new TenantGrantedAuthoritySid(authority));
        }

        return sids;
    }
}
