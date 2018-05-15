package edu.scut.cs.hm.common.security.acl;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import edu.scut.cs.hm.common.security.OwnedByTenant;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Sid;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = TenantPrincipalSid.class)
@JsonSubTypes({@JsonSubTypes.Type(TenantPrincipalSid.class), @JsonSubTypes.Type(TenantGrantedAuthoritySid.class)})
public interface TenantSid extends Sid, OwnedByTenant {
    static TenantSid from(Sid sid) {
        if (sid == null) {
            return null;
        }
        if (sid instanceof TenantSid) {
            return (TenantSid) sid;
        } else if (sid instanceof PrincipalSid) {
            return TenantPrincipalSid.from((PrincipalSid) sid);
        } else if (sid instanceof GrantedAuthoritySid) {
            return TenantGrantedAuthoritySid.from((GrantedAuthoritySid) sid);
        } else {
            throw new IllegalArgumentException("Unsupported sid type: " + sid.getClass());
        }
    }
}
