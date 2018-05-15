package edu.scut.cs.hm.common.security.acl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.scut.cs.hm.common.security.MultiTenancySupport;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.core.GrantedAuthority;

/**
 * A {@link org.springframework.security.acls.domain.GrantedAuthoritySid}
 * extension which supports {@link edu.scut.cs.hm.common.security.acl.TenantSid}
 */
@JsonTypeName("GRANTED_AUTHORITY")
@Getter
@EqualsAndHashCode(callSuper = true)
public class TenantGrantedAuthoritySid extends GrantedAuthoritySid implements TenantSid {
    private final String tenant;

    /**
     * Create {@link TenantGrantedAuthoritySid} from {@link GrantedAuthoritySid}
     * @param sid may be extension of {@link edu.scut.cs.hm.common.security.acl.TenantGrantedAuthoritySid}
     * @return
     */
    public static TenantGrantedAuthoritySid from(GrantedAuthoritySid sid) {
        return new TenantGrantedAuthoritySid(sid.getGrantedAuthority(), MultiTenancySupport.getTenant(sid));
    }

    /**
     * Create {@link TenantGrantedAuthoritySid} from {@link GrantedAuthority}
     * @param ga may be extension of {@link edu.scut.cs.hm.common.security.TenantGrantedAuthority}
     * @return
     */
    public static TenantGrantedAuthoritySid from(GrantedAuthority ga) {
        return new TenantGrantedAuthoritySid(ga.getAuthority(), MultiTenancySupport.getTenant(ga));
    }

    @JsonCreator
    public TenantGrantedAuthoritySid(@JsonProperty("grantedAuthority") String grantedAuthority,
                                     @JsonProperty("tenant") String tenant) {
        super(grantedAuthority);
        this.tenant = tenant;
    }

    public TenantGrantedAuthoritySid(GrantedAuthority grantedAuthority) {
        super(grantedAuthority);
        this.tenant = MultiTenancySupport.getTenant(grantedAuthority);
    }

    @Override
    public String toString() {
        //Do not change below code, it must matches scheme from config file.
        return getGrantedAuthority() + ":" + tenant;
    }
}
