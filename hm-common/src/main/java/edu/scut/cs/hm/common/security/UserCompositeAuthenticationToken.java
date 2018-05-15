package edu.scut.cs.hm.common.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * Token for for credentials  by name, phone or email.
 */
public class UserCompositeAuthenticationToken extends AbstractAuthenticationToken {

    private final UserCompositePrincipal principal;
    private final Object credentials;

    public UserCompositeAuthenticationToken(UserCompositePrincipal principal, Object credentials) {
        super(null);
        this.principal = principal;
        this.credentials = credentials;
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public UserCompositePrincipal getPrincipal() {
        return this.principal;
    }
}