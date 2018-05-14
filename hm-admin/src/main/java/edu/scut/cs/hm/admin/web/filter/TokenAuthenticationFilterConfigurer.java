package edu.scut.cs.hm.admin.web.filter;

import lombok.Data;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Configure TokenAuthenticationFilter
 * @param <H>
 */
@Data
public class TokenAuthenticationFilterConfigurer<H extends HttpSecurityBuilder<H>> extends
        AbstractAuthenticationFilterConfigurer<H, TokenAuthenticationFilterConfigurer<H>, TokenAuthenticationFilter> {

    private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource;
    private RequestMatcher matcher;

    public TokenAuthenticationFilterConfigurer(TokenAuthenticationFilter authenticationFilter) {
        super(authenticationFilter, null);
    }

    public TokenAuthenticationFilterConfigurer(RequestMatcher requestMatcher,
                                               AuthenticationProvider authenticationProvider) {
        this(new TokenAuthenticationFilter(requestMatcher, authenticationProvider));
        this.matcher = requestMatcher;
    }

    @Override
    protected RequestMatcher createLoginProcessingUrlMatcher(String loginProcessingUrl) {
        return matcher;
    }

    @Override
    public void configure(H http) throws Exception {
        TokenAuthenticationFilter tokenFilter = getAuthenticationFilter();
        if (authenticationDetailsSource != null) {
            tokenFilter.setAuthenticationDetailsSource(authenticationDetailsSource);
        }
        tokenFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));
        tokenFilter.setAuthenticationSuccessHandler(new AuthenticationStubSuccessHandler());
        SessionAuthenticationStrategy sessionAuthenticationStrategy = http.getSharedObject(SessionAuthenticationStrategy.class);
        if(sessionAuthenticationStrategy != null) {
            tokenFilter.setSessionAuthenticationStrategy(sessionAuthenticationStrategy);
        }

        TokenAuthenticationFilter filter = postProcess(tokenFilter);
        filter.setContinueChainAfterSuccessfulAuthentication(true);
        http.addFilterBefore(filter, BasicAuthenticationFilter.class);
    }

    public static class AuthenticationStubSuccessHandler implements AuthenticationSuccessHandler {
        @Override public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                                      Authentication authentication) throws IOException, ServletException {

        }
    }
}
