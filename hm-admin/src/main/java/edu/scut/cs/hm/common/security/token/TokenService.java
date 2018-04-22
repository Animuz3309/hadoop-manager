package edu.scut.cs.hm.common.security.token;

/**
 * Service which manages tokens.
 * Token can be identified by it {@link TokenData#getKey()} value.
 */
public interface TokenService {

    /**
     * Create and persist new token
     * @param config
     * @return
     */
    TokenData createToken(TokenConfiguration config);

    /**
     * Get specified token if it exists and valid
     * @param token
     * @return
     */
    TokenData getToken(String token);

    /**
     * Remove specified token
     * @param token
     */
    void removeToken(String token);

    /**
     * Remove all username tokens
     * @param username
     */
    void removeUserTokens(String username);

}
