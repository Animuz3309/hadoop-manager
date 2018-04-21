package edu.scut.cs.hm.common.security.acl.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;
import edu.scut.cs.hm.common.security.MultiTenancySupport;
import edu.scut.cs.hm.common.security.acl.TenantSid;
import edu.scut.cs.hm.common.utils.UUIDs;
import lombok.Data;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable source for acl.
 * We use classes instead of ifaces because need correct serialization to json.
 * For example entry_type:entry_objId = owner_sid@owner_tenant, permissions[revoke user1@user1_tenant R,  grant ROLE_ADMIN@admin CRUD]
 */
@Data
public class AclSource {

    @Data
    public static class Builder {
        private final Map<String, AceSource> entries = new LinkedHashMap<>();
        private ObjectIdentityData objectIdentity;
        private TenantSid owner;
        private AclSource parentAcl;
        private boolean entriesInheriting;

        public Builder from(AclSource acl) {
            if (acl == null) {
                return null;
            }
            setEntries(acl.getEntries());
            setObjectIdentity(acl.getObjectIdentity());
            setOwner(acl.getOwner());
            setParentAcl(acl.getParentAcl());
            setEntriesInheriting(acl.isEntriesInheriting());
            return this;
        }

        public Builder addEntry(AceSource entry) {
            String id = entry.getId();
            if(id == null) {
                // this is new entry
                id = newId();
                entry = AceSource.builder().from(entry).id(id).build();
            }
            this.entries.put(id, entry);
            return this;
        }

        private String newId() {
            while(true) {
                String id = UUIDs.longUid();
                if(!this.entries.containsKey(id)) {
                    return id;
                }
            }
        }

        public void setEntries(List<AceSource> entriesList) {
            this.entries.clear();
            if (entriesList != null) {
                entriesList.forEach(this::addEntry);
            }
        }

        public void setEntriesMap(Map<Long, AceSource> entriesMap) {
            this.entries.clear();
            if (entriesMap != null) {
                entriesMap.forEach((k, v) -> this.addEntry(v));
            }
        }

        public Builder entries(List<AceSource> entries) {
            setEntries(entries);
            return this;
        }

        public Builder objectIdentity(ObjectIdentityData objectIdentity) {
            setObjectIdentity(objectIdentity);
            return this;
        }

        public Builder owner(TenantSid owner) {
            setOwner(owner);
            return this;
        }

        public Builder parentAcl(AclSource parentAcl) {
            setParentAcl(parentAcl);
            return this;
        }

        public Builder entriesInheriting(boolean entriesInheriting) {
            setEntriesInheriting(entriesInheriting);
            return this;
        }

        public AclSource build() {
            return new AclSource(this);
        }
    }

    private final Map<String, AceSource> entriesMap;
    private final ObjectIdentityData objectIdentity;
    private final TenantSid owner;
    private final AclSource parentAcl;
    private final boolean entriesInheriting;

    @JsonCreator
    protected AclSource(Builder b) {
        Assert.notNull(b.objectIdentity, "Object Identity required");
        Assert.notNull(b.owner, "Owner required");
        this.owner = b.owner;
        Assert.notNull(MultiTenancySupport.getTenant(this.owner), "Tenant of owner is null");
        this.objectIdentity = b.objectIdentity;
        this.parentAcl = b.parentAcl;
        this.entriesInheriting = b.entriesInheriting;
        // we must save order of  ACEs
        this.entriesMap = Collections.unmodifiableMap(new LinkedHashMap<>(b.entries));
    }

    @JsonIgnore
    public Map<String, AceSource> getEntriesMap() {
        return entriesMap;
    }

    public List<AceSource> getEntries() {
        return ImmutableList.copyOf(this.entriesMap.values());
    }

    public static Builder builder() {
        return new Builder();
    }
}
