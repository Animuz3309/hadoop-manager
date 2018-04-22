package edu.scut.cs.hm.admin.security.authentication;

import edu.scut.cs.hm.common.security.SuccessAuthProcessor;
import edu.scut.cs.hm.common.security.UserCompositeAuthenticationToken;
import edu.scut.cs.hm.common.security.UserCompositePrincipal;
import edu.scut.cs.hm.common.security.UserIdentifiersDetailsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Authentication based on principal of Username Password
 * @see org.springframework.security.authentication.UsernamePasswordAuthenticationToken
 * @see org.springframework.security.authentication.AuthenticationProvider
 */
@Slf4j
@AllArgsConstructor
public class UserCompositeAuthProvider implements AuthenticationProvider {

    private final UserIdentifiersDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final SuccessAuthProcessor authProcessor;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UserCompositeAuthenticationToken token = convert(authentication);
        UserDetails userDetails = userDetailsService.loadUserByIdentifiers(token.getPrincipal());
        String presentedPassword = authentication.getCredentials().toString();
        if (!passwordEncoder.matches(presentedPassword, userDetails.getPassword())) {
            log.debug("Authentication failed: password does not match stored value for principal " + token.getPrincipal());
            throw new BadCredentialsException("Bad credentials");
        }
        return authProcessor.createSuccessAuth(authentication, userDetails);
    }

    private UserCompositeAuthenticationToken convert(Authentication authentication) {
        UserCompositeAuthenticationToken auth;
        if(authentication instanceof UsernamePasswordAuthenticationToken) {
            UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
            try {
                auth = new UserCompositeAuthenticationToken(UserCompositePrincipal.builder().username(token.getName()).build(), token.getCredentials());
            } catch (RuntimeException e) {
                AuthenticationException ae;
                if(e instanceof AuthenticationException) {
                    ae = (AuthenticationException) e;
                } else {
                    ae = new BadCredentialsException("", e);
                }
                throw ae;
            }
        } else if(authentication instanceof UserCompositeAuthenticationToken) {
            auth = (UserCompositeAuthenticationToken)authentication;
        } else {
            throw new BadCredentialsException("Unsupported token type");
        }
        return auth;
    }
    @Override
    public boolean supports(Class<?> authentication) {
        return UserCompositeAuthenticationToken.class.equals(authentication) ||
                UsernamePasswordAuthenticationToken.class.equals(authentication);
    }
}
