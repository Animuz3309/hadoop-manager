package edu.scut.cs.hm.admin.web.api;

import edu.scut.cs.hm.admin.security.acl.ProvidersAclService;
import edu.scut.cs.hm.admin.service.UserStorage;
import edu.scut.cs.hm.admin.web.model.user.UiUser;
import edu.scut.cs.hm.common.security.Authorities;
import edu.scut.cs.hm.common.security.AuthoritiesService;
import edu.scut.cs.hm.common.security.UserIdentifiersDetailsService;
import edu.scut.cs.hm.common.security.acl.AbstractAclService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@Secured(Authorities.ADMIN_ROLE)
@RequestMapping(value = "/api/users", produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserApi {

    private final UserIdentifiersDetailsService usersService;
    private final UserStorage userStorage;
    private final AuthoritiesService authoritiesService;
    private final PasswordEncoder passwordEncoder;
    private final AbstractAclService aclService;
    private final ProvidersAclService providersAclService;

    @Secured(Authorities.USER_ROLE)
    @RequestMapping(value = "/current", method = RequestMethod.GET)
    public UiUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        return UiUser.fromDetails(userDetails);
    }
}
