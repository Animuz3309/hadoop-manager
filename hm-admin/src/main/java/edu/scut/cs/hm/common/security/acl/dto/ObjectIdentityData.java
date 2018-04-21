package edu.scut.cs.hm.common.security.acl.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;
import edu.scut.cs.hm.common.security.acl.AclUtils;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.ObjectIdentity;

import java.io.Serializable;

/**
 * An extension of {@link org.springframework.security.acls.domain.ObjectIdentityImpl} <p/>
 * Add some metadata for properly serialization ObjectIdentityImpl <p/>
 * Also sometime it must be a key for json object value, therefore we must supports serialization of it into string.
 */
public final class ObjectIdentityData extends ObjectIdentityImpl {

    /**
     * Create {@link ObjectIdentityData} from string
     * @see AclUtils#fromId(String)
     * @param id
     * @return
     */
    @JsonCreator
    public static ObjectIdentityData from(String id) {
        return AclUtils.fromId(id);
    }

    /**
     * Create {@link ObjectIdentityData} from extension of {@link org.springframework.security.acls.model.ObjectIdentity}
     * @param objectIdentity
     * @return
     */
    public static ObjectIdentityData from(ObjectIdentity objectIdentity) {
        if(objectIdentity == null || objectIdentity instanceof ObjectIdentityData) {
            return (ObjectIdentityData) objectIdentity;
        }
        return new ObjectIdentityData(objectIdentity.getType(), objectIdentity.getIdentifier());
    }

    /**
     *
     * @param type the type of domain object instance
     * @param identifier the id of the domain object instance
     */
    @JsonCreator
    public ObjectIdentityData(@JsonProperty("type") String type,
                              @JsonProperty("identifier")Serializable identifier) {
        super(type, validateId(identifier));
    }

    private static Serializable validateId(Serializable id) {
        if (AclUtils.isSupportedId(id)) {
            return id;
        }
        throw new IllegalArgumentException("Unsupported type of identifier: " + id.getClass());
    }

    @Override
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    public Serializable getIdentifier() {
        return super.getIdentifier();
    }

    /**
     * serialize {@link ObjectIdentityData} object to a json string, sometimes as the key in json
     * @return
     */
    @JsonValue
    public String asString() {
        return AclUtils.toId(this);
    }

}
