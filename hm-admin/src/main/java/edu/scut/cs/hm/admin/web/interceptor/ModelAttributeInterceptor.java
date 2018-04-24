package edu.scut.cs.hm.admin.web.interceptor;

import edu.scut.cs.hm.admin.web.model.UiHeader;
import edu.scut.cs.hm.admin.web.model.UiUser;
import edu.scut.cs.hm.common.security.ExtendedUserDetailsImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;

public class ModelAttributeInterceptor extends HandlerInterceptorAdapter {

    private final UserDetailsService userDetailsService;

    private UiHeader uiHeader;
    private ExtendedUserDetailsImpl.Builder userBuilder;

    public ModelAttributeInterceptor(UserDetailsService userDetailsService, UiHeader uiHeader) {
        Assert.notNull(userDetailsService, "userDetailsService is null");
        this.userDetailsService = userDetailsService;
        this.uiHeader = uiHeader;
        this.userBuilder = ExtendedUserDetailsImpl.builder();
    }

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView) throws Exception {
        if (modelAndView == null) {
            return;
        }

        String viewName = modelAndView.getViewName();
        uiHeader.setViewName(viewName);

        UiUser uiUser;
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            userBuilder = userBuilder.from(userDetailsService.loadUserByUsername(principal.getName()));
        }
        uiUser = UiUser.fromDetails(userBuilder.build());

        modelAndView.addObject("head", uiHeader);
        modelAndView.addObject("user", uiUser);
    }
}
