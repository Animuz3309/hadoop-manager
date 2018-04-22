package edu.scut.cs.hm.common.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An implementation of username details with tenant support
 * @see edu.scut.cs.hm.common.security.ExtendedUserDetails
 */
@Cacheable
@Getter
@EqualsAndHashCode(of = {"tenant", "email", "title", "username"})
@ToString
public class ExtendedUserDetailsImpl implements ExtendedUserDetails, Comparable<UserDetails> {

    @Setter
    @Getter
    public static class Builder implements ExtendedUserDetails {
        // below configurer are from ExtendedUserDetails (Customize)
        private String tenant;
        private String email;
        private String title;
        // below configurer are from UserDetails (Spring Security)
        private final Set<GrantedAuthority> authorities = new HashSet<>();
        private String password;
        private String username;
        private boolean accountNonExpired = true;
        private boolean credentialsNonExpired = true;
        private boolean accountNonLocked = true;
        private boolean enabled = true;

        /**
         * Make a Builder from other UserDetails.
         * UserDetails may be the instance of {@link ExtendedUserDetails} or {@link UserDetails}
         * @param other
         * @return
         */
        public Builder from(UserDetails other) {
            if (other == null) {
                return this;
            }
            if (other instanceof ExtendedUserDetails) {
                ExtendedUserDetails eother = (ExtendedUserDetails) other;
                setTenant(eother.getTenant());
                setEmail(eother.getEmail());
                setTitle(eother.getTitle());
            } else {
                setTenant(MultiTenancySupport.getTenant(other));
            }

            setAuthorities(other.getAuthorities());
            setPassword(other.getPassword());
            setUsername(other.getUsername());
            setAccountNonExpired(other.isAccountNonExpired());
            setCredentialsNonExpired(other.isCredentialsNonExpired());
            setAccountNonLocked(other.isAccountNonLocked());
            setEnabled(other.isEnabled());
            return this;
        }

        public Builder tenant(String tenant) {
            setTenant(tenant);
            return this;
        }

        public Builder email(String email) {
            setEmail(email);
            return this;
        }

        public Builder title(String title) {
            setTitle(title);
            return this;
        }

        public Builder addAuthority(GrantedAuthority authority) {
            this.authorities.add(GrantedAuthorityImpl.convert(authority));
            return this;
        }

        public Builder authorities(Collection<? extends GrantedAuthority> authorities) {
            setAuthorities(authorities);
            return this;
        }

        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
        public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
            this.authorities.clear();
            if(authorities != null) {
                this.authorities.addAll(authorities.stream()
                                .map(GrantedAuthorityImpl::convert)
                                .collect(Collectors.toList()));
            }
        }

        public Builder password(String password) {
            setPassword(password);
            return this;
        }

        public Builder username(String username) {
            setUsername(username);
            return this;
        }

        public Builder accountNonExpired(boolean accountNonExpired) {
            setAccountNonExpired(accountNonExpired);
            return this;
        }

        public Builder credentialsNonExpired(boolean credentialsNonExpired) {
            setCredentialsNonExpired(credentialsNonExpired);
            return this;
        }

        public Builder accountNonLocked(boolean accountNonLocked) {
            setAccountNonLocked(accountNonLocked);
            return this;
        }

        public Builder enabled(boolean enabled) {
            setEnabled(enabled);
            return this;
        }

        public ExtendedUserDetailsImpl build() {
            return new ExtendedUserDetailsImpl(this);
        }
    }

    private final String tenant;
    private final String email;
    private final String title;
    private final Set<GrantedAuthority> authorities;
    private final String password;
    private final String username;
    private final boolean accountNonExpired;
    private final boolean credentialsNonExpired;
    private final boolean accountNonLocked;
    private final boolean enabled;

    @JsonCreator
    public ExtendedUserDetailsImpl(Builder builder) {
        this.tenant = builder.tenant;
        this.email = builder.email;
        this.title = builder.title;
        this.authorities = Collections.unmodifiableSet(new HashSet<>(builder.authorities));
        this.password = builder.password;
        this.username = builder.username;
        this.accountNonExpired = builder.accountNonExpired;
        this.credentialsNonExpired = builder.credentialsNonExpired;
        this.accountNonLocked = builder.accountNonLocked;
        this.enabled = builder.enabled;
    }

    @Override
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Create a builder
     * @return
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a builder from a UserDetails object
     * @param other
     * @return
     */
    public static Builder builder(UserDetails other) {
        final Builder builder = builder();
        builder.from(other);
        return builder;
    }

    /**
     * From a UserDetails object create an ExtendedUserDetailsImpl object
     * @param other
     * @return
     */
    public static ExtendedUserDetailsImpl from(UserDetails other) {
        if (other instanceof ExtendedUserDetailsImpl) {
            return (ExtendedUserDetailsImpl) other;
        }
        if (other instanceof ExtendedUserDetailsImpl.Builder) {
            return ((ExtendedUserDetailsImpl.Builder) other).build();
        }
        return builder(other).build();
    }

    /**
     * Compare two UserDetails object by username
     * @param o
     * @return if this username is null return -1 (null is not greater), else just compare two username
     */
    @Override
    public int compareTo(UserDetails o) {
        return ObjectUtils.compare(this.username, o.getUsername(), false);
    }
}
