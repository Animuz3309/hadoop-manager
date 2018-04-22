package edu.scut.cs.hm.admin.security.userdetails;

import edu.scut.cs.hm.admin.config.configurer.PropertyUserDetailsServiceConfigurer;
import edu.scut.cs.hm.common.security.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;

import java.util.*;

import static edu.scut.cs.hm.admin.config.configurer.PropertyUserDetailsServiceConfigurer.UserConfig;

/**
 * Just keep a Map to store username details from {@link PropertyUserDetailsServiceConfigurer}
 * and can't manage users like {@link org.springframework.security.provisioning.UserDetailsManager} <p/>
 * In order to add pre-defined username in Spring environment
 */
@Slf4j
public class ConfigurableUserDetailService implements UserIdentifiersDetailsService {
    private final Map<String, ExtendedUserDetails> detailsMap;

    public ConfigurableUserDetailService(PropertyUserDetailsServiceConfigurer configurer) {
        Map<String, ExtendedUserDetails> detailsMap = new HashMap<>();

        // fetch admin username details from configurer
        String rootTenant = MultiTenancySupport.ROOT_TENANT;
        ExtendedUserDetailsImpl admin = ExtendedUserDetailsImpl.builder()
                .username("admin")
                .title("Administrator")
                .password(configurer.getAdminPassword())
                .tenant(rootTenant)
                .addAuthority(new GrantedAuthorityImpl(Authorities.ADMIN_ROLE, rootTenant))
                .build();

        // add admin to detailsMap
        detailsMap.put(admin.getUsername(), admin);

        Map<String, UserConfig> users = configurer.getUsers();
        if (users != null) {
            for (Map.Entry<String, UserConfig> e: users.entrySet()) {
                ExtendedUserDetailsImpl.Builder ub = ExtendedUserDetailsImpl.builder();
                parseUserName(ub, e, rootTenant);
                UserConfig uc = e.getValue();
                ub.setEmail(uc.getEmail());
                ub.setPassword(uc.getPassword());
                ub.setTitle(uc.getTitle());
                Set<String> roles = uc.getRoles();
                if (roles != null) {
                    for (String authority: roles) {
                        ub.addAuthority(parseAuthority(authority, ub.getTenant()));
                    }
                }
                ExtendedUserDetailsImpl details = ub.build();
                ExtendedUserDetails old = detailsMap.put(ub.getUsername(), details);
                if(old != null) {
                    log.warn("Override \n old={} with \n new={}", old, details);
                }
            }
        }
        this.detailsMap = Collections.unmodifiableMap(detailsMap);

    }

    private static GrantedAuthority parseAuthority(String token, String defaultTenant) {
        String[] arr = StringUtils.split(token, "@");
        String name;
        String tenant;
        if(arr == null) {
            name = token;
            tenant = defaultTenant;
        } else {
            name = arr[0];
            tenant = arr[1];
        }
        return Authorities.fromName(name, tenant);
    }

    private void parseUserName(ExtendedUserDetailsImpl.Builder ub, Map.Entry<String, UserConfig> e, String defaultTenant) {
        String key = e.getKey();
        String[] arr = StringUtils.split(key, "@");
        String username, tenant;
        if (arr == null) {
            username = key;
            tenant = e.getValue().getTenant();
        } else {
            username = arr[0];
            tenant = arr[1];
        }

        if (tenant == null) {
            tenant = defaultTenant;
        }

        ub.setUsername(username);
        ub.setTenant(tenant);
    }

    @Override
    public Collection<ExtendedUserDetails> getUsers() {
        return detailsMap.values();
    }

    @Override
    public ExtendedUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ExtendedUserDetails details = detailsMap.get(username);
        if (details == null) {
            throw new UsernameNotFoundException("'" + username + "' is not found");
        }
        return details;
    }

    @Override
    public ExtendedUserDetails loadUserByIdentifiers(UserIdentifiers identifiers) throws UsernameNotFoundException {
        return loadUserByUsername(identifiers.getUsername());
    }
}
