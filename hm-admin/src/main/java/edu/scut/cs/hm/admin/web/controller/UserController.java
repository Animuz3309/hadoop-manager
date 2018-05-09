package edu.scut.cs.hm.admin.web.controller;

import edu.scut.cs.hm.admin.security.acl.ProvidersAclService;
import edu.scut.cs.hm.admin.service.UserStorage;
import edu.scut.cs.hm.admin.web.model.user.UiUser;
import edu.scut.cs.hm.admin.web.model.user.UiUserUpdate;
import edu.scut.cs.hm.common.security.Authorities;
import edu.scut.cs.hm.common.security.ExtendedUserDetails;
import edu.scut.cs.hm.common.security.ExtendedUserDetailsImpl;
import edu.scut.cs.hm.common.security.UserIdentifiersDetailsService;
import edu.scut.cs.hm.model.HttpException;
import edu.scut.cs.hm.model.user.UserRegistration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Collection;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@Secured(Authorities.ADMIN_ROLE)
@RequestMapping("/users")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserController {
    static final String MODEL_ATTR_USERS = "users";
    static final String MODEL_ATTR_USER = "user";

    static final String VIEW_USER_LIST = "user/list";
    static final String VIEW_USER_DETAILS = "user/details";
    static final String VIEW_USER_FORM = "user/form";

    private final UserIdentifiersDetailsService usersService;
    private final UserStorage userStorage;
    private final PasswordEncoder passwordEncoder;
    private final ProvidersAclService providersAclService;

    @RequestMapping(method = GET)
    public String overview(ModelMap model) {
        Collection<ExtendedUserDetails> users = usersService.getUsers();
        model.put(MODEL_ATTR_USERS, users.stream().map(UiUser::fromDetails).collect(Collectors.toList()));
        return VIEW_USER_LIST;
    }

    @RequestMapping(value = "/{user}", method = GET)
    public String showUserDetails(@PathVariable("user") String username, ModelMap model) {
        ExtendedUserDetails user = getUserDetails(username);
        model.put(MODEL_ATTR_USER, UiUser.fromDetails(user));
        return VIEW_USER_DETAILS;
    }

    @RequestMapping(value = "/{user}/form", method = GET)
    public String showUpdateUserDetailsForm(@PathVariable("user") String username, ModelMap model) {
        ExtendedUserDetails user = getUserDetails(username);
        model.put(MODEL_ATTR_USER, UiUser.fromDetails(user));
        return VIEW_USER_FORM;
    }

    @PreAuthorize("#username == authentication.name || hasRole('ADMIN')")
    @RequestMapping(value = "/{user}", method = POST)
    public RedirectView setUserDetails(@PathVariable("user") String username, UiUserUpdate user) {
        user.setUsername(username);
        String password = user.getPassword();
        // we must encode password
        if (password != null && !UiUser.PWD_STUB.equals(password)) {
            String encodedPwd = passwordEncoder.encode(password);
            user.setPassword(encodedPwd);
        }
        final ExtendedUserDetails source;
        {
            // we load user because it can be defined in different sources,
            // but must stored into userStorage
            ExtendedUserDetails eud = null;
            try {
                eud = usersService.loadUserByUsername(username);
            } catch (UsernameNotFoundException e) {
                //is a usual case
            }
            source = eud;
        }
        UserRegistration reg = userStorage.update(username, (ur) -> {
            ExtendedUserDetails details = ur.getDetails();
            if(details == null && source != null) {
                // if details is null than user Storage does not have this user before
                // and we can transfer our user into it
                details = source;
            }
            ExtendedUserDetailsImpl.Builder builder = ExtendedUserDetailsImpl.builder(details);
            user.toBuilder(builder);
            ur.setDetails(builder);
        });
        return new RedirectView("");
    }

    private ExtendedUserDetails getUserDetails(String username) {
        ExtendedUserDetails user;
        try {
            user = usersService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            user = null;
        }
        if(user == null) {
            throw new HttpException(HttpStatus.NOT_FOUND, "Can not find user with name: " + username);
        }
        return user;
    }

}
