package edu.scut.cs.hm.admin.web.filter;

import edu.scut.cs.hm.common.security.token.TokenException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter td do with request has 'X-Auth-Token' in http head or 'token' in http parameter
 */
@Slf4j
public class TokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    public final static String X_AUTH_TOKEN = "X-Auth-Token";   // in http header
    public final static String TOKEN = "token";                 // in http parameter

    private final AuthenticationProvider authenticationProvider;
    @Setter private boolean continueChainAfterSuccessfulAuthentication;


    public TokenAuthenticationFilter(RequestMatcher requiresAuthenticationRequestMatcher,
                                     AuthenticationProvider authenticationProvider) {
        super(requiresAuthenticationRequestMatcher);
        this.authenticationProvider = authenticationProvider;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        String token = request.getHeader(X_AUTH_TOKEN);
        if (token == null) {
            token = request.getParameter(TOKEN);
        }
        log.debug("Trying to authenticate username by auth token method, Token: {}", token);
        try {
            return processTokenAuthentication(token, getDetails(request));
        } catch (TokenException e) {
            throw new BadCredentialsException(e.getMessage(), e.getCause());
        } catch (Exception e) {
            throw new UsernameNotFoundException(e.getMessage(), e.getCause());
        }
    }

    private Authentication processTokenAuthentication(String token, Object details) {
        final PreAuthenticatedAuthenticationToken authenticationToken =
                new PreAuthenticatedAuthenticationToken(token, null);
        return authenticationProvider.authenticate(authenticationToken);
    }

    private Object getDetails(HttpServletRequest request) {
        return authenticationDetailsSource.buildDetails(request);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult);
        if (continueChainAfterSuccessfulAuthentication) {
            chain.doFilter(request, response);
        }
    }
}
