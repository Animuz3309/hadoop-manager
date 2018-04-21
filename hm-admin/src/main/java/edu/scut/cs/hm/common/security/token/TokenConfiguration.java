package edu.scut.cs.hm.common.security.token;

import lombok.Data;

@Data
public final class TokenConfiguration {
    private String username;
    private String deviceHash;
}
