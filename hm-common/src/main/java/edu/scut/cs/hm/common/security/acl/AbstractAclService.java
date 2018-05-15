package edu.scut.cs.hm.common.security.acl;

import edu.scut.cs.hm.common.security.acl.dto.AclSource;
import org.springframework.security.acls.model.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link org.springframework.security.acls.model.AclService} <p/>
 * Provides retrieval of {@link Acl} instances.
 */
public abstract class AbstractAclService implements AclService {

    @Override
    public List<ObjectIdentity> findChildren(ObjectIdentity parentIdentity) {
        return Collections.emptyList();
    }

    @Override
    public Acl readAclById(ObjectIdentity object) throws NotFoundException {
        return readAclById(object, Collections.emptyList());
    }

    @Override
    public Map<ObjectIdentity, Acl> readAclsById(List<ObjectIdentity> objects) throws NotFoundException {
        return readAclsById(objects, Collections.emptyList());
    }

    @Override
    public Map<ObjectIdentity, Acl> readAclsById(List<ObjectIdentity> objects, List<Sid> sids) throws NotFoundException {
        Map<ObjectIdentity, Acl> map = new HashMap<>();
        for(ObjectIdentity object: objects) {
            Acl acl = readAclById(object, sids);
            map.put(object, acl);
        }
        return map;
    }

    /**
     * used for publish permissions to ui
     * @param oid
     * @return
     */
    public abstract AclSource getAclSource(ObjectIdentity oid);
}
