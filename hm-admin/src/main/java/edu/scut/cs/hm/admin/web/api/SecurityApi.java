package edu.scut.cs.hm.admin.web.api;

import edu.scut.cs.hm.admin.security.SecuredType;
import edu.scut.cs.hm.admin.security.acl.ProvidersAclService;
import edu.scut.cs.hm.admin.web.model.UiResult;
import edu.scut.cs.hm.admin.web.model.acl.UiAclUpdate;
import edu.scut.cs.hm.admin.web.model.user.UiRole;
import edu.scut.cs.hm.common.security.Authorities;
import edu.scut.cs.hm.common.security.AuthoritiesService;
import edu.scut.cs.hm.common.security.MultiTenancySupport;
import edu.scut.cs.hm.common.security.UserIdentifiersDetailsService;
import edu.scut.cs.hm.common.security.acl.AbstractAclService;
import edu.scut.cs.hm.common.security.acl.dto.AceSource;
import edu.scut.cs.hm.common.security.acl.dto.AclSource;
import edu.scut.cs.hm.common.security.acl.dto.ObjectIdentityData;
import edu.scut.cs.hm.common.utils.Sugar;
import edu.scut.cs.hm.model.NotFoundException;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@Secured(Authorities.ADMIN_ROLE)
@RequestMapping(value = "/api", produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SecurityApi {
    private final UserIdentifiersDetailsService usersService;
    private final AuthoritiesService authoritiesService;
    private final AbstractAclService aclService;
    private final ProvidersAclService providersAclService;

    @Secured(Authorities.USER_ROLE)
    @RequestMapping(value = "/roles/", method = RequestMethod.GET)
    public Collection<UiRole> getGroups() {
        Collection<GrantedAuthority> authorities = authoritiesService.getAuthorities();
        return authorities.stream().map(UiRole::fromAuthority).collect(Collectors.toList());
    }

    @RequestMapping(path = "/acl/", method = RequestMethod.GET)
    public List<String> getSecuredTypes() {
        return Arrays.stream(SecuredType.values()).map(SecuredType::name).collect(Collectors.toList());
    }

    @ApiOperation("Batch update of ACLs. Not that, due to we can not guarantee consistency of batch update, we return " +
            "map with result of updating each ACL.")
    @RequestMapping(path = "/acl/", method = RequestMethod.POST)
    public Map<String, UiResult> setAcls(@RequestBody Map<ObjectIdentityData, UiAclUpdate> acls) {
        // we save order of calls
        Map<String, UiResult> results = new LinkedHashMap<>();
        acls.forEach((oid, aclSource) -> {
            try {
                providersAclService.updateAclSource(oid, as -> updateAcl(aclSource, as));
            } catch (org.springframework.security.acls.model.NotFoundException e) {
                throw new NotFoundException(e);
            }
        });
        return results;
    }

    @RequestMapping(path = "/acl/{type}/{id}", method = RequestMethod.GET)
    public AclSource getAcl(@PathVariable("type") String type, @PathVariable("id") String id) {
        SecuredType securedType = SecuredType.valueOf(type);
        ObjectIdentity oid = securedType.id(id);
        try {
            return aclService.getAclSource(oid);
        } catch (org.springframework.security.acls.model.NotFoundException e) {
            throw new NotFoundException(e);
        }
    }

    @RequestMapping(path = "/acl/{type}/{id}", method = RequestMethod.POST)
    public void setAcl(@PathVariable("type") String type, @PathVariable("id") String id, @RequestBody UiAclUpdate aclSource) {
        SecuredType securedType = SecuredType.valueOf(type);
        ObjectIdentity oid = securedType.id(id);
        try {
            providersAclService.updateAclSource(oid, as -> updateAcl(aclSource, as));
        } catch (org.springframework.security.acls.model.NotFoundException e) {
            throw new NotFoundException(e);
        }
    }

    private boolean updateAcl(UiAclUpdate aclSource, AclSource.Builder as) {
        Sugar.setIfNotNull(as::setOwner, MultiTenancySupport.fixTenant(aclSource.getOwner()));
        List<UiAclUpdate.UiAceUpdate> list = aclSource.getEntries();
        Map<String, AceSource> existed = as.getEntries();
        if(list.isEmpty()) {
            return false;
        }
        for (UiAclUpdate.UiAceUpdate entry : list) {
            String aceId = entry.getId();
            AceSource ace = aceId == null ? null : existed.get(aceId);
            if (ace == null) {
                // add new
                AceSource.Builder b = AceSource.builder();
                // note that id may be null, it is normal
                b.setId(aceId);
                Sugar.setIfNotNull(b::setAuditFailure, entry.getAuditFailure());
                Sugar.setIfNotNull(b::setAuditSuccess, entry.getAuditSuccess());
                b.setSid(entry.getSid());
                b.setGranting(entry.getGranting());
                b.setPermission(entry.getPermission());
                as.addEntry(b.build());
                continue;
            }
            if (entry.isDelete()) {
                existed.remove(aceId);
                continue;
            }
            // modify existed
            AceSource.Builder b = AceSource.builder().from(ace);
            b.setId(aceId);
            Sugar.setIfNotNull(b::setAuditFailure, entry.getAuditFailure());
            Sugar.setIfNotNull(b::setAuditSuccess, entry.getAuditSuccess());
            Sugar.setIfNotNull(b::setSid, entry.getSid());
            Sugar.setIfNotNull(b::setGranting, entry.getGranting());
            Sugar.setIfNotNull(b::setPermission, entry.getPermission());
            as.addEntry(b.build());
        }
        return true;
    }
}
