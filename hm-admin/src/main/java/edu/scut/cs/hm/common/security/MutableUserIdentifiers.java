package edu.scut.cs.hm.common.security;

/**
 * Mutable iface for object contains unique username identifiers
 * <p>
 *     This iface extends {@link UserIdentifiers} and represents username identifiers can be changed
 * </p>
 */
public interface MutableUserIdentifiers extends UserIdentifiers {

    /**
     * Username aka login
     * @param username
     */
    void setUsername(String username);

    /**
     * User email
     * @param email
     */
    void setEmail(String email);
}
