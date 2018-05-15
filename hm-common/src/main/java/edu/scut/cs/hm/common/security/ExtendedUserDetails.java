package edu.scut.cs.hm.common.security;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * Customer iface to extends Spring Security <code>UserDetails</code>
 * <p>
 *     Provide username information.
 *     note. Spring Security UserDetails provides {@link #getUsername()} method
 * </p>
 * @see org.springframework.security.core.userdetails.UserDetails
 */
public interface ExtendedUserDetails extends UserDetails, OwnedByTenant {

    /**
     * username's title
     * @return
     */
    String getTitle();

    /**
     * username's email
     * @return
     */
    String getEmail();
}
