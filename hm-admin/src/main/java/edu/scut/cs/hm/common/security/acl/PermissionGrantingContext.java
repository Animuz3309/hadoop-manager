package edu.scut.cs.hm.common.security.acl;

import edu.scut.cs.hm.common.security.MultiTenancySupport;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.acls.model.Sid;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * Context with some permission granting strategy parameters which used in {@link PermissionGrantingJudge}
 */
final class PermissionGrantingContext {
    private final TenantBasedPermissionGrantedStrategy strategy;
    @Getter private final Sid ownerSid;                             // owner sid of entry want to access
    @Getter private final String ownerTenant;                       // owner sid's tenant of entry want to access
    private final Set<String> currentTenants = new TreeSet<>();     // current sid's tenant include children tenant of it
    private final Set<String> currentTenantWrapped = Collections.unmodifiableSet(currentTenants);
    private final String currentDefaultTenant;

    @Getter
    private Sid currentSid; // current sid of what want to access an entry (the sid may be principal or granted authority)
    @Getter @Setter
    private boolean hasAces;

    PermissionGrantingContext(TenantBasedPermissionGrantedStrategy strategy, Sid ownerSid, String currentDefaultTenant) {
        this.strategy = strategy;
        this.ownerSid = ownerSid;
        this.ownerTenant = this.strategy.getTenantFromSid(this.ownerSid);
        if (Objects.equals(ownerTenant, MultiTenancySupport.NO_TENANT)) {
            throw new IllegalArgumentException("ACL.owner.tenantId == MultiTenancySupport.NO_TENANT");
        }
        this.currentDefaultTenant = currentDefaultTenant;
    }

    void setCurrentSid(Sid currentSid) {
        this.currentSid = currentSid;
        currentTenants.clear();
        String currentSidTenant = strategy.getTenantFromSid(this.currentSid);
        if (Objects.equals(currentSidTenant, MultiTenancySupport.NO_TENANT)) {
            currentSidTenant = currentDefaultTenant;
        }
        currentTenants.add(currentSidTenant);
        strategy.tenantService.getChildTenants(currentSidTenant, currentTenants);
    }

    Set<String> getCurrentTenants() {
        return currentTenantWrapped;
    }
}
