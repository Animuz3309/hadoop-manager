package edu.scut.cs.hm.admin.security.acl;

import com.google.common.collect.ImmutableMap;
import edu.scut.cs.hm.common.security.acl.AbstractAclService;
import edu.scut.cs.hm.common.security.acl.AclUtils;
import edu.scut.cs.hm.common.security.acl.dto.AclImpl;
import edu.scut.cs.hm.common.security.acl.dto.AclSource;
import lombok.Data;
import org.springframework.security.acls.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A configurable AbstractAclService, to get Acl and AclSource
 * @see AbstractAclService
 */
public class ConfigurableAclService extends AbstractAclService {

    @Data
    public static class Builder {
        private PermissionGrantingStrategy pgs;
        private final Map<String, AclSource> acls = new HashMap<>();

        public Builder putAcl(AclSource acl) {
            String id = AclUtils.toId(acl.getObjectIdentity());
            return putAcl(id, acl);
        }

        public Builder putAcl(String id, AclSource acl) {
            this.acls.put(id, acl);
            return this;
        }

        public Builder acls(Map<String, AclSource> acls) {
            setAcls(acls);
            return this;
        }

        public void setAcls(Map<String, AclSource> acls) {
            this.acls.clear();
            this.acls.putAll(acls);
        }

        public Builder permissionGrantingStrategy(PermissionGrantingStrategy permissionGrantingStrategy) {
            setPgs(permissionGrantingStrategy);
            return this;
        }

        public ConfigurableAclService build() {
            return new ConfigurableAclService(this);
        }
    }

    private final PermissionGrantingStrategy pgs;
    private final Map<String, AclSource> acls;

    public static Builder builder() {
        return new Builder();
    }

    private ConfigurableAclService(Builder builder) {
        this.pgs = builder.pgs;
        this.acls = ImmutableMap.copyOf(builder.acls);
    }

    @Override
    public Acl readAclById(ObjectIdentity object, List<Sid> sids) throws NotFoundException {
        AclSource acl = getAclSource(object);
        return new AclImpl(this.pgs, acl);
    }

    @Override
    public AclSource getAclSource(ObjectIdentity oid) {
        String id = AclUtils.toId(oid);
        AclSource acl = acls.get(id);
        if (acl == null) {
            // if we don't have object acl, then we use per type acl
            acl = acls.get(AclUtils.toTypeId(oid));
        }
        if (acl == null) {
            throw new NotFoundException("Acl not found for: " + oid);
        }
        return acl;
    }
}
