package edu.scut.cs.hm.admin.web.model.acl;

import edu.scut.cs.hm.common.security.acl.TenantSid;
import edu.scut.cs.hm.common.security.acl.dto.PermissionData;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UiAclUpdate {

    @Data
    public static class UiAceUpdate {
        protected boolean delete = false;
        protected String id;
        protected TenantSid sid;
        protected Boolean granting;
        protected PermissionData permission;
        protected Boolean auditFailure;
        protected Boolean auditSuccess;
    }

    private final List<UiAceUpdate> entries = new ArrayList<>();
    private TenantSid owner;
}
