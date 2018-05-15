package edu.scut.cs.hm.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;

public class SecurityUtils {

    public static final ExtendedUserDetails USER_ANONYMOUS = ExtendedUserDetailsImpl.builder()
            .title("Anonymous")
            .username("anonymous")
            .tenant(MultiTenancySupport.ANONYMOUS_TENANT)
            .build();
    public static final ExtendedUserDetails USER_SYSTEM = ExtendedUserDetailsImpl.builder()
            .title("System")
            .username("system")
            .tenant(MultiTenancySupport.ROOT_TENANT)
            .accountNonLocked(false)
            .addAuthority(Authorities.ADMIN)
            .build();
    public static final Authentication AUTH_SYSTEM = AuthenticationImpl.builder()
            .authenticated(true)
            .authorities(USER_SYSTEM.getAuthorities())
            .principal(USER_SYSTEM)
            .name(USER_SYSTEM.getUsername())
            .build();


    /**
     * Validate {@link edu.scut.cs.hm.common.security.UserIdentifiers} object. <p/>
     * It check that object contains at least one not null identify value
     *
     * @param ui
     */
    public static void validate(UserIdentifiers ui) {
        if (!StringUtils.hasText(ui.getUsername())
                && !StringUtils.hasText(ui.getEmail())) {
            throw new RuntimeException(ui.getClass().getSimpleName() +
                    " must construct with at least one of non null fields");
        }
    }
}
