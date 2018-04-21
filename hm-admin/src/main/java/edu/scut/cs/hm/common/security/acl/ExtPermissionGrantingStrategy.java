package edu.scut.cs.hm.common.security.acl;

import edu.scut.cs.hm.common.security.acl.dto.PermissionData;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.acls.model.Sid;

import java.util.List;

/**
 * Customization of the logic for determining whether a permission or permissions
 * are granted to a particular sid or sids by an {@link Acl}.
 * @see org.springframework.security.acls.model.PermissionGrantingStrategy
 */
public interface ExtPermissionGrantingStrategy extends PermissionGrantingStrategy {

    /**
     * Collecting ACE entries and default permissions to single permission of specified sids
     * @param acl
     * @param sids
     * @return
     */
    PermissionData getPermission(Acl acl, List<Sid> sids);
}
