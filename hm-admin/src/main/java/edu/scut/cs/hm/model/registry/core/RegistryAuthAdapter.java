package edu.scut.cs.hm.model.registry.core;

import lombok.Data;
import org.springframework.http.HttpHeaders;

/**
 * Fetch/Set Registry auth information from {@link HttpHeaders}
 */
public interface RegistryAuthAdapter {
    void handle(AuthContext ctx);

    /**
     * fetch/set auth info from http
     */
    @Data
    class AuthContext {
        private final HttpHeaders requestHeaders;
        private final HttpHeaders responseHeaders;
        /**
         * "Www-Authenticate" request header value
         */
        private final String authenticate;
    }
}
