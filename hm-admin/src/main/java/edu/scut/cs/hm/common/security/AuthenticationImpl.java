package edu.scut.cs.hm.common.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * The implementation of {@link org.springframework.security.core.Authentication}
 */
@Getter
@ToString
@EqualsAndHashCode(of = {"authenticated", "credentials", "name", "authorities"})
public class AuthenticationImpl implements Authentication {

    @Getter
    @Setter
    public static class Builder implements Authentication {
        private boolean authenticated;
        private Object principal;
        private Object details;
        private Object credentials;
        private String name;
        private final Set<GrantedAuthority> authorities = new HashSet<>();

        public Builder() {}

        public Builder authenticated(boolean authenticated) {
            setAuthenticated(authenticated);
            return this;
        }

        @Override
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
        public Object getPrincipal() {
            return principal;
        }

        public void setPrincipal(Object principal) {
            this.principal = convertPrincipal(principal);
        }

        private Object convertPrincipal(Object principal) {
            if (principal instanceof User) {
                return ExtendedUserDetailsImpl.builder((UserDetails) principal).build();
            }
            return principal;
        }

        public Builder principal(Object principal) {
            setPrincipal(principal);
            return this;
        }

        @Override
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
        public Object getDetails() {
            return details;
        }

        public Builder details(Object details) {
            setDetails(details);
            return this;
        }

        @Override
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
        public Object getCredentials() {
            return credentials;
        }

        public Builder credentials(Object credentials) {
            setCredentials(credentials);
            return this;
        }

        public Builder name(Object credentials) {
            setName(name);
            return this;
        }

        @Override
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }

        public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
            this.authorities.clear();
            if(authorities != null) {
                for(GrantedAuthority authority: authorities) {
                    this.authorities.add(GrantedAuthorityImpl.convert(authority));
                }
            }
        }

        public Builder authorities(Collection<? extends GrantedAuthority> authorities) {
            setAuthorities(authorities);
            return this;
        }

        public AuthenticationImpl build() {
            return new AuthenticationImpl(this);
        }
    }

    // implement org.springframework.security.core.Authentication
    private final boolean authenticated;
    private final Object principal;
    private final Object details;
    private final Object credentials;
    private final Set<GrantedAuthority> authorities;

    // implement java.security.Principal
    private final String name;

    public static Builder builder() {
        return new Builder();
    }

    public static Builder from(Authentication authentication) {
        final Builder builder = builder();
        builder.setAuthenticated(authentication.isAuthenticated());
        builder.setPrincipal(authentication.getPrincipal());
        builder.setDetails(authentication.getDetails());
        builder.setCredentials(authentication.getCredentials());
        builder.setName(authentication.getName());
        builder.setAuthorities(authentication.getAuthorities());
        return builder;
    }

    @JsonCreator
    public AuthenticationImpl(Builder b) {
        this.authenticated = b.authenticated;
        this.authorities = Collections.unmodifiableSet(new HashSet<>(b.authorities));
        this.credentials = b.credentials;
        this.details = b.details;
        this.principal = b.principal;
        this.name = b.name;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    @Override
    public Object getCredentials() {
        return credentials;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    @Override
    public Object getDetails() {
        return details;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if(this.authenticated != isAuthenticated) {
            throw new IllegalArgumentException("changing of data is not supported");
        }
    }

    @JsonProperty(value = "@class")
    private String getClassName() {
        return AuthenticationImpl.class.getName();
    }
}
