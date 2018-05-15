package edu.scut.cs.hm.common.security.token;

import org.springframework.util.Assert;

/**
 * Token utils
 */
public final class TokenUtils {
    private TokenUtils() {}

    public static String getTypeFromKey(String key) {
        if(key == null || key.isEmpty()) {
            throw new TokenException("Token is null or empty");
        }
        int i = key.indexOf(":");

        if (i <= 0) {
            throw new TokenException("Can't get type from token");
        }
        return key.substring(0, i);
    }

    public static String getKeyWithTypeAndToken(String type, String token) {
        return type + ":" + token;
    }
}
