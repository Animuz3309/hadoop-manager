package edu.scut.cs.hm.common.security.token;

/**
 * The representation of token
 */
public interface TokenData {
    /**
     * User name
     * @return
     */
    String getUsername();

    /**
     * Device hash, also may depend from user name, ip, mac address. It used for binding token to
     * specific device, if you don't want this behavior, then leave this property null.
     * @return
     */
    String getDeviceHash();

    /**
     * Token type, token may have different type, so there can be different {@link TokenService} to serve
     * token of different type
     * @return
     */
    String getType();

    /**
     * Token key
     * @return
     */
    String getKey();

    /**
     * Token creation time
     * @return
     */
    long getCreationTime();
}
