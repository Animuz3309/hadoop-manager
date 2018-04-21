package edu.scut.cs.hm.common.security;

/**
 * Common iface for object contains unique user identifiers
 * <p>
 *     Here is two identifiers 'username' and 'email', and at last one of them must be not null
 * </p>
 */
public interface UserIdentifiers {

    /**
     * username aka login
     * @return
     */
    String getUsername();

    /**
     * email
     * @return
     */
    String getEmail();
}
