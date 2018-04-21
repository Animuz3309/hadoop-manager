package edu.scut.cs.hm.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Process authentication for user.
 */
public interface SuccessAuthProcessor {

    /**
     * Create success credentials from source Authentication and UserDetails
     * @param source souce authentication
     * @param userDetails UserDetails
     * @return
     */
    Authentication createSuccessAuth(Authentication source, UserDetails userDetails);
}
