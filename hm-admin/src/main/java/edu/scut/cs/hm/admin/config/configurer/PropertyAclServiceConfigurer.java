package edu.scut.cs.hm.admin.config.configurer;

import com.google.common.base.Splitter;
import edu.scut.cs.hm.admin.security.acl.AclServiceConfigurer;
import edu.scut.cs.hm.admin.security.acl.ConfigurableAclService;
import edu.scut.cs.hm.common.security.acl.AclUtils;
import edu.scut.cs.hm.common.security.acl.TenantGrantedAuthoritySid;
import edu.scut.cs.hm.common.security.acl.TenantPrincipalSid;
import edu.scut.cs.hm.common.security.acl.TenantSid;
import edu.scut.cs.hm.common.security.acl.dto.AceSource;
import edu.scut.cs.hm.common.security.acl.dto.AclSource;
import edu.scut.cs.hm.common.security.acl.dto.Action;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.model.Permission;
import org.springframework.util.StringUtils;

import java.util.Iterator;
import java.util.Map;

/**
 * Use *.properties to config AbstractAclService
 */
@ConfigurationProperties("hm.security.acl")
@Data
public class PropertyAclServiceConfigurer implements AclServiceConfigurer {

    private static final Splitter ACL_SPLITTER = Splitter.on(',').trimResults();
    private static final Splitter ACE_SPLITTER = Splitter.on(' ').trimResults();
    private Map<String, String> store;

    @Override
    public void configure(ConfigurableAclService.Builder builder) {
        if (store != null) {
            for (Map.Entry<String, String> e: store.entrySet()) {
                String id = e.getKey();
                String value = e.getValue();
                AclSource.Builder asb = parse(id, value);
                builder.putAcl(asb.build());
            }
        }
    }

    /**
     * Parser expression like below value from property:
     * <pre>
     *                                                  values from {@link Action#getLetter()}
     *                                                                        \___
     *                                                                        /   \
     * hm.security.acl.list[CONTAINER@cont123] = system@root, grant sam@root R    , grant ROLE_USER@root CRUDE
     * hm.security.acl.list[CONTAINER@cont123] = system@root, grant ROLE_USER@root CRUDE
     *                      \_type__/ \_id__/    \____/ \__/  \___/ \_______/ \_/
     *                                         owner^    /      /       /      ^tenant
     *                                                  /      /       /
     *                                           tenant^      /   role or username (role always start with 'ROLE_')
     *                                    'grant' or 'revoke'^
     * </pre>
     * Above we omit id type, just use pattern like 'type@id' not 'type:idtype:id' ('type:s:id' s means id type is string)
     * see {@link AclUtils#fromId(String)} when id type is null than default id type is 's'(string) <p/>
     * Because *.properties file doesn't allow ':' in key, so we use '@' instead as divider
     * @param id
     * @param value
     * @return
     */
    private static AclSource.Builder parse(String id, String value) {
        AclSource.Builder asb = AclSource.builder();
        id = id.replace('@', ':');  // *.properties file doesn't allow ':' in key
        asb.setObjectIdentity(AclUtils.fromId(id));

        Iterator<String> it = ACL_SPLITTER.split(value).iterator();
        if (it.hasNext()) {
            asb.setOwner(parseSid(it.next()));
            while (it.hasNext()) {
                asb.addEntry(parseAce(it.next()));
            }
        }

        return asb;
    }

    /**
     * Grant ROLE_USER@root CRUDE
     * @param token
     * @return
     */
    private static AceSource parseAce(String token) {
        Iterator<String> it = ACE_SPLITTER.split(token).iterator();
        String grantStr = it.next();
        AceSource.Builder asb = AceSource.builder();
        switch (grantStr) {
            case "grant":
                asb.granting(true);
                break;
            case "revoke":
                asb.granting(false);
                break;
            default:
                throw new IllegalArgumentException("rule: " + token + " must start with 'grant' or 'revoke'");
        }
        try {
            TenantSid sid = parseSid(it.next());
            asb.sid(sid);
        } catch (Exception e) {
            throw new IllegalArgumentException("rule: " + token + " contains invalid sid", e);
        }

        String perms = it.next();
        asb.permission(parsePerms(perms));
        if (it.hasNext()) {
            throw new IllegalArgumentException("Too long rule: " + token + " we expect only three space delimited items");
        }
        return asb.build();
    }

    private static Permission parsePerms(String perms) {

        final int length = perms.length();
        if(length > 32) {
            throw new IllegalArgumentException("Too long permission expression: " + perms + " it must be shortest than 32 chars.");
        }
        Permission perm;
        if(length == 1) {
            perm = parseLetter(perms.charAt(0));
        } else {
            CumulativePermission cp = new CumulativePermission();
            for(int i = 0; i < length; ++i) {
                cp.set(parseLetter(perms.charAt(i)));
            }
            perm = cp;
        }
        return perm;
    }

    private static Action parseLetter(char c) {
        Action perm = Action.fromLetter(c);
        if(perm == null) {
            throw new IllegalArgumentException("Unknown action letter : " + c);
        }
        return perm;
    }


    /**
     * Grant system@root or ROLE_ADMIN@root
     * @param token
     * @return
     */
    private static TenantSid parseSid(String token) {
        String[] arr = StringUtils.split(token, "@");
        if (arr == null) {
            throw new IllegalArgumentException("Can not parse sid: " + token + " expect something like 'text@text'");
        }
        if (token.startsWith("ROLE_")) {
            return new TenantGrantedAuthoritySid(arr[0], arr[1]);
        } else {
            return new TenantPrincipalSid(arr[0], arr[1]);
        }
    }

}
