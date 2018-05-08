package edu.scut.cs.hm.admin.web.controller;

import edu.scut.cs.hm.admin.security.acl.ProvidersAclService;
import edu.scut.cs.hm.admin.service.UserStorage;
import edu.scut.cs.hm.admin.web.model.UiUser;
import edu.scut.cs.hm.common.security.Authorities;
import edu.scut.cs.hm.common.security.ExtendedUserDetails;
import edu.scut.cs.hm.common.security.UserIdentifiersDetailsService;
import edu.scut.cs.hm.common.security.acl.AbstractAclService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collection;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@Secured(Authorities.ADMIN_ROLE)
@RequestMapping("/users")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserController {
    static final String MODEL_ATTR_USERS = "users";
    static final String VIEW_USER_LIST = "user/list";

    private final UserIdentifiersDetailsService usersService;
    private final UserStorage userStorage;
    private final PasswordEncoder passwordEncoder;
    private final ProvidersAclService providersAclService;

    @RequestMapping(value = "/", method = GET)
    public String getUsers(ModelMap model) {
        Collection<ExtendedUserDetails> users = usersService.getUsers();
        model.put(MODEL_ATTR_USERS, users.stream().map(UiUser::fromDetails).collect(Collectors.toList()));
        return VIEW_USER_LIST;
    }
}
