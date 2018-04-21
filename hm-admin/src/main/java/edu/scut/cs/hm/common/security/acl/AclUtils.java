package edu.scut.cs.hm.common.security.acl;

import com.google.common.collect.ImmutableMap;
import edu.scut.cs.hm.common.security.acl.dto.AccessControlEntryImpl;
import edu.scut.cs.hm.common.security.acl.dto.AceSource;
import edu.scut.cs.hm.common.security.acl.dto.ObjectIdentityData;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * ACL utils
 */
public final class AclUtils {

    private AclUtils() {}

    private static class TypeSupport {
        private final String id;
        private final Class<?> type;
        private final Function<String, Object> reader;  // function to read value from input string

        TypeSupport(String id, Class<?> type, Function<String, Object> reader) {
            this.id = id;
            this.type = type;
            this.reader = reader;
        }
    }

    // support type of id(identifier) is String, Integer, Long
    private static final Map<Object, TypeSupport> SUPPORTED_TYPES;
    static {
        ImmutableMap.Builder<Object, TypeSupport> b = ImmutableMap.builder();
        TypeSupport[] ts = {
                new TypeSupport("s", String.class, a -> a),
                new TypeSupport("i", Integer.class, Integer::valueOf),
                new TypeSupport("l", Long.class, Long::valueOf)
        };
        for (TypeSupport t : ts) {
            b.put(t.id, t);
            b.put(t.type, t);
        }
        SUPPORTED_TYPES = b.build();
    }

    /**
     * Judge the type of id is supported or not
     * @param id identifier
     * @return
     */
    public static boolean isSupportedId(Serializable id) {
        if (id == null) {
            return true;
        }
        Class<?> type = id.getClass();
        return SUPPORTED_TYPES.get(type) != null;
    }

    /**
     * Serialize {@link ObjectIdentity} to id string, like 'type:s:id'
     * @see #fromId(String)
     * @param object
     * @return
     */
    public static String toId(ObjectIdentity object) {
        Assert.notNull(object, "ObjectIdentity is null");
        Object idSrc = object.getIdentifier();
        Assert.notNull(idSrc, "Identifier is null");
        String type = object.getType();
        return toId(type, idSrc);
    }

    private static String toId(String type, Object id) {
        Assert.isTrue(type.indexOf(':') < 0, "type contains ':'. ");
        if (id == null
                || (id instanceof String && ((String) id).isEmpty())) {
            return type + ":";
        }
        return type + ":" + getIdType(id) + ":" + id.toString();
    }

    private static String getIdType(Object id) {
        if (id == null) {
            return "";
        }
        Class<?> clazz = id.getClass();
        TypeSupport support = SUPPORTED_TYPES.get(clazz);
        Assert.notNull(support, "Unsupported id type: " + clazz);
        return support.id;
    }

    /**
     * Serialize {@link ObjectIdentity}'s type to id string, like 'type:'
     * @see #fromId(String)
     * @param object
     * @return
     */
    public static String toTypeId(ObjectIdentity object) {
        Assert.notNull(object, "ObjectIdentity is null");
        String type = object.getType();
        return toId(type, null);
    }

    /**
     * Deserialize string to {@link ObjectIdentityData}
     * @param oid string of {@link ObjectIdentityData} instance, like "type:s:id" <p/>
     *            The 'type' in "type:s:id" is the type of the domain object that {@link ObjectIdentityData}'s identifier
     *            refer to <p/>
     *            The 's' in "type:s:id" is the supported type's id of identifier that defined in {@link AclUtils}
     *            The 'id' in "type:s:id" is the value of identifier
     * @return
     */
    public static ObjectIdentityData fromId(String oid) {
        Assert.notNull(oid, "oid is null");
        int typeEnd = oid.indexOf(':');
        Assert.isTrue(typeEnd > 0, "Bad string. Expect like: 'type:s:id'.");
        String type = oid.substring(0, typeEnd);

        String idType = "s";
        String idStr;
        String second = oid.substring(typeEnd + 1);
        int idTypeEnd = second.indexOf(':');
        if (idTypeEnd >= 0) { // has type of id(identifier)
            idStr = second.substring(idTypeEnd + 1);
            idType = second.substring(0, idTypeEnd);
        } else { // just has string value of id(identifier)
            idStr = second;
        }
        Object id = "";
        if (!idStr.isEmpty()) {
            TypeSupport typeSupport = SUPPORTED_TYPES.get(idType);
            Assert.notNull(typeSupport, "Unsupported id type: " + idType);
            id = typeSupport.reader.apply(idStr);
        }
        return new ObjectIdentityData(type, (Serializable) id);
    }

    public static void buildEntries(Acl acl, Collection<?> from, Consumer<AccessControlEntry> to) {
        for (Object obj: from) {
            AccessControlEntry ace;
            if (obj instanceof AceSource) {
                ace = new AccessControlEntryImpl.Builder().from((AccessControlEntry) obj).acl(acl).build();
            } else if (obj instanceof AccessControlEntryImpl.Builder) {
                ace = ((AccessControlEntryImpl.Builder) obj).acl(acl).build();
            } else if (obj instanceof  AccessControlEntry) {
                ace = (AccessControlEntry) obj;
            } else {
                throw new IllegalArgumentException(obj + " must be an instance of " +
                        AccessControlEntry.class + " or it's builder");
            }
            to.accept(ace);
        }
    }
}
