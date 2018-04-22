package edu.scut.cs.hm.admin.filter;

import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;

/**
 * RequestMatcher(strategy to match an <tt>HttpServletRequest</tt>.) for TokenAuthenticationFilter
 */
public class TokenHeaderRequestMatcher implements RequestMatcher {
    @Override
    public boolean matches(HttpServletRequest request) {
        return request.getHeader(TokenAuthenticationFilter.X_AUTH_TOKEN) != null
                || request.getParameter(TokenAuthenticationFilter.TOKEN) != null;
    }
}
