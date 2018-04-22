package edu.scut.cs.hm.admin.web;

import edu.scut.cs.hm.admin.web.model.UiHeader;
import edu.scut.cs.hm.admin.web.model.UiUser;
import edu.scut.cs.hm.common.security.ExtendedUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PublicAdvice {

    private final UserDetailsService userDetailsService;
    @Value("${spring.application.name:Hadoop Manager Tool}")
    String appName;

    @ModelAttribute("header")
    public UiHeader uiHeader() {
        UiHeader uiHeader = new UiHeader();
        uiHeader.setAppName(appName);
        return uiHeader;
    }

    @ModelAttribute("user")
    public UiUser uiUser(Principal principal) {
        if (principal == null) {
            return null;
        }
        String username = principal.getName();
        ExtendedUserDetails userDetails = (ExtendedUserDetails) userDetailsService.loadUserByUsername(username);
        return UiUser.fromDetails(userDetails);
    }

}
