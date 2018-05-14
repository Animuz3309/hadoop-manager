package edu.scut.cs.hm.admin.web.api;

import edu.scut.cs.hm.admin.security.acl.ProvidersAclService;
import edu.scut.cs.hm.admin.service.UserStorage;
import edu.scut.cs.hm.admin.web.model.user.UiRole;
import edu.scut.cs.hm.admin.web.model.user.UiRoleUpdate;
import edu.scut.cs.hm.admin.web.model.user.UiUser;
import edu.scut.cs.hm.admin.web.model.user.UiUserUpdate;
import edu.scut.cs.hm.common.security.Authorities;
import edu.scut.cs.hm.common.security.ExtendedUserDetails;
import edu.scut.cs.hm.common.security.ExtendedUserDetailsImpl;
import edu.scut.cs.hm.common.security.UserIdentifiersDetailsService;
import edu.scut.cs.hm.common.security.acl.AclUtils;
import edu.scut.cs.hm.common.security.acl.dto.AclSource;
import edu.scut.cs.hm.common.security.acl.dto.ObjectIdentityData;
import edu.scut.cs.hm.common.utils.ExtendedAssert;
import edu.scut.cs.hm.model.HttpException;
import edu.scut.cs.hm.model.user.UserRegistration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@Secured(Authorities.ADMIN_ROLE)
@RequestMapping(value = "/api/users", produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UsersApi {

    /**
     * can get user from config file
     * @see edu.scut.cs.hm.admin.security.userdetails.ConfigurableUserDetailService
     */
    private final UserIdentifiersDetailsService usersService;
    private final UserStorage usersStorage;
    private final PasswordEncoder passwordEncoder;
    private final ProvidersAclService providersAclService;

    @RequestMapping(value = "/", method = GET)
    public Collection<UiUser> getUsers() {
        Collection<ExtendedUserDetails> users = usersService.getUsers();
        return users.stream().map(UiUser::fromDetails).collect(Collectors.toList());
    }

    @RequestMapping(value = "/{user}", method = GET)
    public UiUser getUser(@PathVariable("user") String username) {
        ExtendedUserDetails user = getUserDetails(username);
        return UiUser.fromDetails(user);
    }

    private ExtendedUserDetails getUserDetails(String username) {
        ExtendedUserDetails user;
        try {
            user = usersService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            user = null;
        }

        if (user == null) {
            throw new HttpException(HttpStatus.NOT_FOUND, "Can not find user with name: " + username);
        }

        return user;
    }

    @PreAuthorize("#username == authentication.name || hasRole('ADMIN')")
    @RequestMapping(value = "/{user}", method = POST)
    public UiUser setUser(@PathVariable("user") String username, @RequestBody UiUserUpdate user) {
        user.setUsername(username);
        String password = user.getPassword();
        if (password != null && !UiUser.PWD_STUB.equals(password)) {
            String encodedPwd = passwordEncoder.encode(password);
            user.setPassword(encodedPwd);
        }
        final ExtendedUserDetails source;
        {
            ExtendedUserDetails eud = null;
            try {
                eud = usersService.loadUserByUsername(username);
            } catch (UsernameNotFoundException e) {
                // is usual case
            }
            source = eud;
        }

        UserRegistration reg = usersStorage.update(username, (ur) -> {
            ExtendedUserDetails details = ur.getDetails();
            if (details == null && source != null) {
                // if details is null than user Storage dose not have this user before
                // and we can transfer our user into it
                details = source;
            }
            ExtendedUserDetailsImpl.Builder builder = ExtendedUserDetailsImpl.builder(details);
            user.toBuilder(builder);
            ur.setDetails(builder);
        });
        return UiUser.fromDetails(reg.getDetails());
    }

    @RequestMapping(value = "/{user}", method = DELETE)
    public void deleteUser(@PathVariable("user") String username) {
        usersStorage.remove(username);
    }

    @Secured(Authorities.USER_ROLE)
    @RequestMapping(value = "/current", method = GET)
    public UiUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        return UiUser.fromDetails(userDetails);
    }

    @RequestMapping(value = "/{user}/roles/", method = RequestMethod.GET)
    public List<UiRole> getUserRoles(@PathVariable("user") String username) {
        ExtendedUserDetails details = getUserDetails(username);
        List<UiRole> roles = details.getAuthorities().stream().map(UiRole::fromAuthority).collect(Collectors.toList());
        roles.sort(null);
        return roles;
    }

    @RequestMapping(value = "/{user}/roles/", method = RequestMethod.POST)
    public List<UiRole> updateUserRoles(@PathVariable("user") String username, @RequestBody List<UiRoleUpdate> updatedRoles) {
        UserRegistration ur = usersStorage.get(username);
        ExtendedAssert.notFound(ur, "Can not find user: " + username);
        if(!updatedRoles.isEmpty()) {
            ur.update((r) -> {
                ExtendedUserDetailsImpl.Builder builder = ExtendedUserDetailsImpl.builder(ur.getDetails());
                UiUserUpdate.updateRoles(updatedRoles, builder);
                r.setDetails(builder);
            });
        }
        ExtendedUserDetails details = ur.getDetails();
        List<UiRole> roles = details.getAuthorities().stream().map(UiRole::fromAuthority).collect(Collectors.toList());
        roles.sort(null);
        return roles;
    }

    @RequestMapping(path = "/{user}/acls/", method = RequestMethod.GET)
    public Map<ObjectIdentityData, AclSource> getUserAcls(@PathVariable("user") String username) {
        Map<ObjectIdentityData, AclSource> map = new HashMap<>();
        providersAclService.getAcls((a) -> {
            if(!AclUtils.isContainsUser(a, username)) {
                return;
            }
            // we must serialize as our object, it allow save it as correct string
            map.put(a.getObjectIdentity(), a);
        });
        return map;
    }
}
