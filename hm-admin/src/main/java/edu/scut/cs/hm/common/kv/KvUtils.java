package edu.scut.cs.hm.common.kv;

public final class KvUtils {
    private KvUtils() {}

    public static boolean predicate(String pattern, String key) {
        int last = pattern.length();
        final int keyLen = key.length();
        if (pattern.charAt(last - 1) == '*') {
            //we check that key start with pattern, but without '*'
            last--;
        }
        if (pattern.charAt(last - 1) == '/' && keyLen == last - 1) {
            //when operation act on current node then key does not contain a end slash
            last--;
        }
        return key.regionMatches(0, pattern, 0, last);
    }
}
