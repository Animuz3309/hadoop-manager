package edu.scut.cs.hm.common.security;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;

/**
 * Service which allow us to load username by name, or by its identifiers. <p/>
 * Customer iface extends Spring Security <code>UserDetailsService</code>
 * @see org.springframework.security.core.userdetails.UserDetailsService
 *
 */
public interface UserIdentifiersDetailsService extends UserDetailsService {

    Collection<ExtendedUserDetails> getUsers();

    ExtendedUserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    /**
     * Load <code>ExtendedUserDetails</code> by <code>UserIdentifiers</code>
     * @param identifiers
     * @return
     * @throws UsernameNotFoundException
     */
    ExtendedUserDetails loadUserByIdentifiers(UserIdentifiers identifiers) throws UsernameNotFoundException;
}
