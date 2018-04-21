package edu.scut.cs.hm.common.security.token;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;

@Slf4j
@Data
@Builder(builderClassName = "Builder")
public class TokenValidatorImpl implements TokenValidtor {
    private final Cache cache;
    private final TokenService tokenService;
    private final long expireAfterInSec;
    private final long expireLastAccessInSec;

    @Override
    public TokenData verifyToken(String token, String deviceHash) {
        TokenData tokenData;
        try {
            tokenData = tokenService.getToken(token);
        } catch (Exception e) {
            log.error("Error due check token", e);
            throw new TokenException("Token is not valid: " + e.getMessage());
        }
        final long currentTime = System.currentTimeMillis();
        Long lastAccess = getLastAccess(tokenData);
        final boolean ttl = (currentTime - tokenData.getCreationTime()) >= (expireAfterInSec * 1000L);
        final boolean tti = lastAccess == null || (currentTime - lastAccess) >= (expireLastAccessInSec * 1000L);
        boolean expired = ttl && tti;
        if (expired) {
            throw new TokenException("Token '" + token + "' is expired.");
        }
        setLastAccess(tokenData, currentTime);
        return tokenData;
    }

    private void setLastAccess(TokenData tokenData, long currentTime) {
        cache.put(tokenData.getKey(), currentTime);
    }

    private Long getLastAccess(TokenData tokenData) {
        Cache.ValueWrapper wrapper = cache.get(tokenData.getKey());
        return wrapper == null? null : (Long) wrapper.get();
    }

    @Override
    public TokenData verifyToken(String token) {
        return verifyToken(token, null);
    }
}
