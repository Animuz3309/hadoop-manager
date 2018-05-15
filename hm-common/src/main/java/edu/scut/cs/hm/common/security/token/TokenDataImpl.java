package edu.scut.cs.hm.common.security.token;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "key")
public class TokenDataImpl implements TokenData {
    private final String username;
    private final String deviceHash;
    private final String key;
    private final long creationTime;

    @JsonCreator
    public TokenDataImpl(@JsonProperty("username") String username,
                         @JsonProperty("deviceHash") String deviceHash,
                         @JsonProperty("key") String key,
                         @JsonProperty("creationTime") long creationTime) {
        this.username = username;
        this.deviceHash = deviceHash;
        this.key = key;
        this.creationTime = creationTime;
    }

    @Override
    public String getType() {
        return TokenUtils.getTypeFromKey(key);
    }
}
