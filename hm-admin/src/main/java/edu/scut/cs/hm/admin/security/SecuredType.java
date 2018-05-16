package edu.scut.cs.hm.admin.security;

import edu.scut.cs.hm.common.security.Authorities;
import edu.scut.cs.hm.common.security.acl.dto.ObjectIdentityData;
import org.springframework.security.acls.model.ObjectIdentity;

/**
 * Constants for known secured object types. <p/>
 * In some cases system can use class name instead of this.
 */
public enum SecuredType {
    /**
     * a group of docker containers
     */
    CLUSTER,

    /**
     * physical computer node
     */
    NODE,

    /**
     * docker container
     */
    CONTAINER,

    /**
     * Docker service
     */
    SERVICE,

    /**
     * image placed local node
     */
    LOCAL_IMAGE,

    /**
     * image placed on repository
     */
    REMOTE_IMAGE,

    /**
     * docker network to connect each container
     */
    NETWORK,
    ;
    /**
     * It can not be calculated, because it is used for annotation.
     */
    public static final String CLUSTER_ADMIN = "ROLE_CLUSTER_ADMIN";
    private final String adminRole;
    private final String userRole;

    SecuredType() {
        this.adminRole = Authorities.adminOf(name());
        this.userRole = Authorities.userOf(name());
    }

    /**
     * Make {@link ObjectIdentity } for specified id and current type
     * @param id if null then act like {@link #typeId()}
     * @return ObjectIdentity for specified id and type, or only type if id is null
     */
    public ObjectIdentityData id(String id) {
        if (id == null) {
            return typeId();
        }
        return new ObjectIdentityData(name(), id);
    }

    /**
     * Make {@link ObjectIdentity } for current type
     * @see edu.scut.cs.hm.common.security.acl.AclUtils#toTypeId(ObjectIdentity)
     * @return
     */
    public ObjectIdentityData typeId() {
        return new ObjectIdentityData(name(), "");
    }

    public String admin() {
        return adminRole;
    }

    public String user() {
        return userRole;
    }
}
