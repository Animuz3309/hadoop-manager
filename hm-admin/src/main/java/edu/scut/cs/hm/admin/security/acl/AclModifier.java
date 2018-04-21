package edu.scut.cs.hm.admin.security.acl;

import edu.scut.cs.hm.common.security.acl.dto.AclSource;

/**
 * Iface for modifier Acl
 */
public interface AclModifier {
    /**
     * Modify AclSource.Builder
     * @param builder
     * @return
     */
    boolean modify(AclSource.Builder builder);
}
