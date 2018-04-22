package edu.scut.cs.hm.admin.security.authentication;

import edu.scut.cs.hm.common.security.SuccessAuthProcessor;
import edu.scut.cs.hm.common.security.token.TokenData;
import edu.scut.cs.hm.common.security.token.TokenValidator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

/**
 * Authentication based on principal of Token
 */
@Slf4j
@AllArgsConstructor
public class TokenAuthProvider implements AuthenticationProvider {
    private final TokenValidator tokenValidator;
    private final UserDetailsService userDetailsService;
    private final SuccessAuthProcessor authProcessor;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        final TokenData tokenData = fetchToken(authentication);
        if (tokenData != null) {
            final UserDetails userDetails = userDetailsService.loadUserByUsername(tokenData.getUsername());
            log.debug("Token {} is valid; userDetails is {}", tokenData, userDetails);
            return authProcessor.createSuccessAuth(authentication, userDetails);
        } else {
            throw new UsernameNotFoundException("User not found" + authentication.getCredentials());
        }
    }

    private TokenData fetchToken(Authentication authentication) {
        String principal = (String) authentication.getPrincipal();
        if (principal == null) {
            log.warn("principal wasn't passed");
            return null;
        }
        return tokenValidator.verifyToken(principal);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(PreAuthenticatedAuthenticationToken.class);
    }
}
