package edu.scut.cs.hm.common.security;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * Customer iface to extends Spring Security <code>UserDetails</code>
 * <p>
 *     Provide user information.
 *     note. Spring Security UserDetails provides {@link #getUsername()} method
 * </p>
 * @see org.springframework.security.core.userdetails.UserDetails
 */
public interface ExtendedUserDetails extends UserDetails, OwnedByTenant {

    /**
     * user's title
     * @return
     */
    String getTitle();

    /**
     * user's email
     * @return
     */
    String getEmail();
}
