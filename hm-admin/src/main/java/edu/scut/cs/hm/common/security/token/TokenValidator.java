package edu.scut.cs.hm.common.security.token;

/**
 * Token verification service
 */
public interface TokenValidator {

    /**
     * Get specified token if it exists and valid
     * @param token
     * @param deviceHash
     * @return
     */
    TokenData verifyToken(String token, String deviceHash);

    /**
     * Get specified token if it exists and valid
     * @param token
     * @return
     */
    TokenData verifyToken(String token);
}
