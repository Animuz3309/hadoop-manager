package edu.scut.cs.hm.admin.config.configurer;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

public class TokenServiceConfigurer {

    @ConfigurationProperties("hm.token")
    @Data
    public static class TokenValidtorConfigurer {
        /**
         * Timeout in seconds before expiration of token.
         */
        // current value is 24 hours
        private Long expireAfterInSec = 86400L;

        /**
         * Timeout in seconds from last access after which token expires.
         */
        // half hour
        private Long expireLastAccessInSec = 1800L;
    }

    @ConfigurationProperties("hm.token")
    @Data
    public static class SignedTokenServiceConfigurer {

        /**
         * Server secret to generate token
         */
        private String serverSecret = "3153620caaf300c37b345d0c2e8dc3aa322c6d9d";   // 明文

        /**
         * Server integer to generate token
         */
        private Integer serverInteger = 13;

        /**
         * Number of random bytes to generate token
         */
        private Integer pseudoRandomNumberBytes = 32;

        /**
         * Digest algorithm to generate token
         */
        private String digestAlgorithm = "sha1";
    }
}
