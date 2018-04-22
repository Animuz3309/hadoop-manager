package edu.scut.cs.hm.common.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.*;

/**
 * This is a principal which is used for credentials by one of username's identifiers like username, email and so on. <p/>
 * For example username, email is already defined in {@link UserIdentifiers}
 */
@Data
public class UserCompositePrincipal implements UserIdentifiers {
    private final String username;
    private final String email;

    @JsonCreator
    public UserCompositePrincipal(Builder b) {
        this.username = b.username;
        this.email = b.email;
        SecurityUtils.validate(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Data
    public static class Builder implements MutableUserIdentifiers {
        private String username;
        private String email;

        public Builder username(String username) {
            setUsername(username);
            return this;
        }

        public Builder email(String email) {
            setEmail(email);
            return this;
        }

        public UserCompositePrincipal build() {
            return new UserCompositePrincipal(this);
        }
    }
}
