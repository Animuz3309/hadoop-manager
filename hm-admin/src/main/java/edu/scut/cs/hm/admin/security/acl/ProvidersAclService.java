package edu.scut.cs.hm.admin.security.acl;

import edu.scut.cs.hm.common.security.acl.AbstractAclService;
import edu.scut.cs.hm.common.security.acl.dto.AclImpl;
import edu.scut.cs.hm.common.security.acl.dto.AclSource;
import lombok.Getter;
import org.springframework.security.acls.model.*;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

/**
 * Combine and use {@link AclProvider} to provide acl service. <p/>
 * Different objects can produce different {@link AclProvider}s
 */
public class ProvidersAclService extends AbstractAclService {
    private final PermissionGrantingStrategy pgs;
    @Getter private final ConcurrentMap<String, AclProvider> providers = new ConcurrentHashMap<>();

    public ProvidersAclService(PermissionGrantingStrategy permissionGrantingStrategy) {
        this.pgs = permissionGrantingStrategy;
    }

    public void getAcls(Consumer<AclSource> consumer) {
        providers.forEach((k, p) -> {
            p.list(consumer);
        });
    }

    @Override
    public AclSource getAclSource(ObjectIdentity oid) {
        AclProvider provider = getAclProvider(oid);
        AclSource source = provider.provide(oid.getIdentifier());
        if(source == null) {
            throw new NotFoundException("Can not find acl for id : " + oid);
        }
        return source;
    }

    private AclProvider getAclProvider(ObjectIdentity oid) {
        AclProvider provider = providers.get(oid.getType());
        if(provider == null) {
            throw new NotFoundException("Can not find AclProvider for type : " + oid.getType());
        }
        return provider;
    }

    @Override
    public Acl readAclById(ObjectIdentity object, List<Sid> sids) throws NotFoundException {
        AclSource source = getAclSource(object);
        return new AclImpl(this.pgs, source);
    }

    public void updateAclSource(ObjectIdentity oid, AclModifier modifier) {
        AclProvider provider = getAclProvider(oid);
        provider.update(oid.getIdentifier(), modifier);
    }
}
