package edu.scut.cs.hm.model;

import edu.scut.cs.hm.admin.security.acl.AclModifier;
import edu.scut.cs.hm.common.security.acl.dto.AclSource;
import org.springframework.security.acls.model.ObjectIdentity;

/**
 * Control by Security ACL
 */
public interface WithAcl {

    ObjectIdentity getOid();

    AclSource getAcl();

    void updateAcl(AclModifier operator);
}
