package edu.scut.cs.hm.common.security.acl.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import edu.scut.cs.hm.common.security.acl.TenantSid;
import lombok.Data;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.AuditableAccessControlEntry;
import org.springframework.security.acls.model.Permission;
import org.springframework.util.Assert;

import java.util.Objects;

/**
 * Source for access control entry. It cannot be used as {@link AccessControlEntry} because has null Acl {@link #getAcl()}. <p/>
 */
@Data
public class AceSource implements AuditableAccessControlEntry {

    public static class Builder extends AbstractBuilder<Builder> {

        @Override
        public AceSource build() {
            return new AceSource(this);
        }
    }

    @Data
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, defaultImpl = Builder.class)
    public abstract static class AbstractBuilder<T> implements AuditableAccessControlEntry {
        protected String id;
        protected TenantSid sid;
        protected boolean granting;
        protected PermissionData permission;
        protected boolean auditFailure = false;
        protected boolean auditSuccess = false;

        @SuppressWarnings("unchecked")
        protected T thiz() {
            return (T) this;
        }

        public T id(String id) {
            setId(id);
            return thiz();
        }

        public T sid(TenantSid sid) {
            setSid(sid);
            return thiz();
        }

        public T granting(boolean granting) {
            setGranting(granting);
            return thiz();
        }

        public T permission(Permission permission) {
            setPermission(PermissionData.from(permission));
            return thiz();
        }

        public T auditFailure(boolean auditFailure) {
            setAuditFailure(auditFailure);
            return thiz();
        }

        public T auditSuccess(boolean auditSuccess) {
            setAuditSuccess(auditSuccess);
            return thiz();
        }

        public T from(AccessControlEntry entry) {
            this.id = Objects.toString(entry.getId(), null);
            this.sid = TenantSid.from(entry.getSid());
            this.granting = entry.isGranting();
            this.permission = PermissionData.from(entry.getPermission());
            if(entry instanceof AuditableAccessControlEntry) {
                AuditableAccessControlEntry ae = (AuditableAccessControlEntry) entry;
                this.auditFailure = ae.isAuditFailure();
                this.auditSuccess = ae.isAuditSuccess();
            }
            return thiz();
        }

        @Override
        public Acl getAcl() {
            //as planned
            return null;
        }

        public abstract AceSource build();
    }

    protected final String id;
    protected final TenantSid sid;
    protected final boolean granting;
    protected final PermissionData permission;
    protected final boolean auditFailure;
    protected final boolean auditSuccess;

    @JsonCreator
    protected AceSource(AbstractBuilder<?> b) {
        Assert.notNull(b.sid, "Sid required");
        Assert.notNull(b.permission, "Permission required");
        this.id = b.id;
        this.sid = b.sid;
        this.permission = b.permission;
        this.granting = b.granting;
        this.auditSuccess = b.auditSuccess;
        this.auditFailure = b.auditFailure;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonIgnore
    @Override
    public Acl getAcl() {
        //as planned
        return null;
    }
}
