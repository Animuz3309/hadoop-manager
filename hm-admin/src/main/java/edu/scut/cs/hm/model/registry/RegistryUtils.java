package edu.scut.cs.hm.model.registry;

public final class RegistryUtils {
    private RegistryUtils() {
    }

    /**
     * Extract repository name from url.
     * @param url
     * @return
     */
    public static String getNameByUrl(String url) {
        int begin = url.indexOf("://");
        if(begin == -1) {
            return null;
        }
        begin += 3 /* len of '://' */;
        int end = url.indexOf("/", begin);
        return end == -1 ? url.substring(begin) : url.substring(begin, end);
    }
}
