package edu.scut.cs.hm.common.kv;

import com.google.common.base.Strings;

import java.util.Arrays;

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
            //when operation act on current swarmNode then key does not contain a end slash
            last--;
        }
        return key.regionMatches(0, pattern, 0, last);
    }

    /**
     * Utility which correct join path components. <p/>
     * Accept any '/component/' with or without '/' at ends and join they in correct '/component1/component2/.../componentN/' path.
     * @param components
     * @return
     */
    public static String join(String ... components) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < components.length; ++i) {
            String component = components[i];
            if(Strings.isNullOrEmpty(component)) {
                throw new IllegalArgumentException("Null or empty component at " + i + " in " + Arrays.toString(components));
            }
            final int lastChar = sb.length() - 1;
            if(component.charAt(0) != '/' && (lastChar < 0 || sb.charAt(lastChar) != '/')) {
                sb.append('/');
            }
            sb.append(component);
        }
        if(sb.length() > 0 && sb.charAt(sb.length() - 1) != '/') {
            sb.append('/');
        }
        return sb.toString();
    }

    /**
     * Name of first path element after prefix.
     * @param prefix
     * @param path
     * @return name or null
     */
    public static String name(String prefix, String path) {
        String tmp = KvUtils.suffix(prefix, path);
        if(tmp == null) {
            return null;
        }
        int i = tmp.indexOf('/');
        if(i == 0) {
            throw new RuntimeException("Incorrect path: " + path + " and prefix:" + prefix);
        }
        if(i > 0) {
            tmp = tmp.substring(0, i);
        }
        return tmp;
    }

    /**
     * Gives child of specified number enclosure.
     * @param prefix prefix, child start from it
     * @param path path
     * @param childNum number of enclosing
     * @return child or null
     */
    public static String child(String prefix, String path, int childNum) {
        if(!path.startsWith(prefix)) {
            return null;
        }
        int start = prefix.length();
        if(prefix.charAt(start - 1) != '/') {
            start += 1;
        }
        int end;
        while(true) {
            end = path.indexOf('/', start);
            if(childNum <= 0) {
                break;
            }
            if(end < start) {
                return null;
            }
            childNum--;
            start = end + 1;
        }
        if(start == path.length()) {
            return null;
        }
        if(end < start) {
            return path.substring(start);
        }
        return path.substring(start, end);
    }

    /**
     * Return path relative to prefix (its suffix, but without leading slash).
     * @param prefix
     * @param path
     * @return
     */
    public static String suffix(String prefix, String path) {
        if(!path.startsWith(prefix)) {
            return null;
        }

        int end = prefix.length();
        if(path.length() > end && path.charAt(end) == '/') {
            end++;
        }
        return path.substring(end);
    }
}
