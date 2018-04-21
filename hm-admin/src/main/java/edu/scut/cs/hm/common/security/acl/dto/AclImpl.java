package edu.scut.cs.hm.common.security.acl.dto;

import com.google.common.collect.ImmutableList;
import edu.scut.cs.hm.common.security.acl.AclUtils;
import lombok.Getter;
import org.springframework.security.acls.model.*;
import org.springframework.util.Assert;

import java.util.List;

public class AclImpl implements Acl {

    @Getter private final List<AccessControlEntry> entries;
    @Getter private final ObjectIdentity objectIdentity;
    @Getter private final Sid owner;
    @Getter private final Acl parentAcl;
    @Getter private final boolean entriesInheriting;
    private transient PermissionGrantingStrategy pgs;

    public AclImpl(PermissionGrantingStrategy pgs, AclSource b) {
        this.owner = b.getOwner();
        Assert.notNull(this.owner, "Owner required");
        this.objectIdentity = b.getObjectIdentity();
        Assert.notNull(this.objectIdentity, "Object Identity required");
        AclSource parentAcl = b.getParentAcl();
        if (parentAcl != null) {
            this.parentAcl = new AclImpl(pgs, parentAcl);
        } else {
            this.parentAcl = null;
        }
        this.entriesInheriting = b.isEntriesInheriting();
        this.pgs = pgs;
        Assert.notNull(this.pgs, "PermissionGrantingStrategy required");

        ImmutableList.Builder<AccessControlEntry> entriesBuilder = ImmutableList.builder();
        AclUtils.buildEntries(this, b.getEntries(),  entriesBuilder::add);
        this.entries = entriesBuilder.build();
    }

    @Override
    public boolean isGranted(List<Permission> permission, List<Sid> sids, boolean administrativeMode) throws NotFoundException, UnloadedSidException {
        Assert.notEmpty(permission, "Permissions required");
        Assert.notEmpty(sids, "SIDs required");

        if (!this.isSidLoaded(sids)) {
            throw new UnloadedSidException("ACL was not loaded for one or more SID");
        }

        return pgs.isGranted(this, permission, sids, administrativeMode);
    }

    @Override
    public boolean isSidLoaded(List<Sid> sids) {
        return true;
    }
}
