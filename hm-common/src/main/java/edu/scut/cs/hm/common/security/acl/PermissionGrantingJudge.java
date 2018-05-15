package edu.scut.cs.hm.common.security.acl;

import edu.scut.cs.hm.common.security.acl.dto.PermissionData;

public interface PermissionGrantingJudge {
    PermissionData getPermission(PermissionGrantingContext context);
}
