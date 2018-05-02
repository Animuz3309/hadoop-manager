package edu.scut.cs.hm.common.utils;

/**
 * Network address utils
 */
public final class AddressUtils {
    private AddressUtils() {}

    public static boolean isHttps(String url) {
        return url != null && url.startsWith("https://");
    }
}
