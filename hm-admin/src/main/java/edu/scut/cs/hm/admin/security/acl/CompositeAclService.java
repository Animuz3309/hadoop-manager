package edu.scut.cs.hm.admin.security.acl;

import edu.scut.cs.hm.common.security.acl.AbstractAclService;
import edu.scut.cs.hm.common.security.acl.dto.AclSource;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;

import java.util.List;

/**
 * Composite all other AbstractAclService, to get Acl and AclSource
 */
public class CompositeAclService extends AbstractAclService {
    private List<AbstractAclService> services;

    public CompositeAclService(List<AbstractAclService> services) {
        this.services = services;
        this.services.sort(AnnotationAwareOrderComparator.INSTANCE);
    }

    @Override
    public AclSource getAclSource(ObjectIdentity oid) {
        for (AbstractAclService service: services) {
            try {
                AclSource acl = service.getAclSource(oid);
                if (acl != null) {
                    return acl;
                }
            } catch (NotFoundException e) {
                // suppress
            }
        }
        throw new NotFoundException("Acl not found for: " + oid);
    }

    @Override
    public Acl readAclById(ObjectIdentity object, List<Sid> sids) throws NotFoundException {
        for (AbstractAclService service: services) {
            try {
                Acl acl = service.readAclById(object);
                if (acl != null) {
                    return acl;
                }
            } catch (NotFoundException e) {
                // suppress
            }
        }
        throw new NotFoundException("Acl not found for: " + object);
    }
}
