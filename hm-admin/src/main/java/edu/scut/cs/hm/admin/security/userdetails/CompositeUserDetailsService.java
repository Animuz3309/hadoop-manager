package edu.scut.cs.hm.admin.security.userdetails;

import edu.scut.cs.hm.common.security.ExtendedUserDetails;
import edu.scut.cs.hm.common.security.UserIdentifiers;
import edu.scut.cs.hm.common.security.UserIdentifiersDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.*;

/**
 * Composite all other UserDetailsService, and load users from these services
 * @see ConfigurableUserDetailService {@link Ordered#LOWEST_PRECEDENCE}
 */
@Slf4j
public class CompositeUserDetailsService implements UserIdentifiersDetailsService {
    private List<UserIdentifiersDetailsService> services;

    public CompositeUserDetailsService(List<UserIdentifiersDetailsService> services) {
        this.services = new ArrayList<>(services);
        this.services.sort(AnnotationAwareOrderComparator.INSTANCE);
    }

    @Override
    public Collection<ExtendedUserDetails> getUsers() {
        Map<String, ExtendedUserDetails> map = new HashMap<>();
        for (UserIdentifiersDetailsService service: services) {
            Collection<ExtendedUserDetails> users = service.getUsers();
            users.forEach(e -> {
                if (e == null) {
                    log.error("Service {} has null in username store.", service);
                    return;
                }
                // note that services has precedence， so we cannot replace details
                map.putIfAbsent(e.getUsername(), e);
            });
        }
        List<ExtendedUserDetails> list = new ArrayList<>(map.values());
        list.sort(null);
        return list;
    }

    @Override
    public ExtendedUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        for (UserIdentifiersDetailsService service: services) {
            try {
                ExtendedUserDetails details = service.loadUserByUsername(username);
                if (details != null) {
                    return details;
                }
            } catch (UsernameNotFoundException e) {
                //suppress
            }
        }
        throw new UsernameNotFoundException("User name: " + username);
    }

    @Override
    public ExtendedUserDetails loadUserByIdentifiers(UserIdentifiers identifiers) throws UsernameNotFoundException {
        for(UserIdentifiersDetailsService service: services) {
            try {
                ExtendedUserDetails details = service.loadUserByIdentifiers(identifiers);
                if(details != null) {
                    return details;
                }
            } catch (UsernameNotFoundException e) {
                //suppress
            }
        }
        throw new UsernameNotFoundException("User identifiers: " + identifiers);
    }
}
