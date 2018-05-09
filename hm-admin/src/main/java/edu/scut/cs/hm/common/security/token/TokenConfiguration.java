package edu.scut.cs.hm.common.security.token;

import lombok.Data;

/**
 * The configuration for created token.
 */
@Data
public final class TokenConfiguration {
    private String username;
    private String deviceHash;
}
