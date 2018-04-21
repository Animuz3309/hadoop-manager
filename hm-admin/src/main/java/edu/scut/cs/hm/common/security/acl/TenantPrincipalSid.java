package edu.scut.cs.hm.common.security.acl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.scut.cs.hm.common.security.MultiTenancySupport;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

/**
 * A {@link org.springframework.security.acls.domain.PrincipalSid}
 * extension which supports {@link edu.scut.cs.hm.common.security.acl.TenantSid}
 */
@JsonTypeName("PRINCIPAL")
@Getter
@EqualsAndHashCode(callSuper = true)
public class TenantPrincipalSid extends PrincipalSid implements TenantSid {
    private final String tenant;

    /**
     * Create {@link TenantPrincipalSid} from {@link org.springframework.security.acls.domain.PrincipalSid}
     * @param sid  may be extension of {@link edu.scut.cs.hm.common.security.acl.TenantPrincipalSid}
     * @return
     */
    public static TenantPrincipalSid from(PrincipalSid sid) {
        return new TenantPrincipalSid(sid.getPrincipal(), MultiTenancySupport.getTenant(sid));
    }

    /**
     * Create {@link TenantPrincipalSid} from {@link UserDetails}
     * @param userDetails may be extension of {@link edu.scut.cs.hm.common.security.ExtendedUserDetails}
     * @return
     */
    public static TenantPrincipalSid from(UserDetails userDetails) {
        return new TenantPrincipalSid(userDetails.getUsername(), MultiTenancySupport.getTenant(userDetails));
    }

    @JsonCreator
    public TenantPrincipalSid(@JsonProperty("principal") String principal,
                              @JsonProperty("tenant") String tenant) {
        super(principal);
        this.tenant = tenant;
        validate();
    }

    public TenantPrincipalSid(Authentication auth) {
        super(auth);
        this.tenant = MultiTenancySupport.getTenant(auth.getPrincipal());
        validate();
    }

    private void validate() {
        Assert.notNull(this.tenant, "tenant of principal is null");
    }

    @Override
    public String toString() {
        return "TenantPrincipalSid[" + getPrincipal() + ":" + tenant + ']';
    }
}
