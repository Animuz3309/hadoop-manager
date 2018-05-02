package edu.scut.cs.hm.common.utils;

import java.util.function.IntPredicate;

/**
 * My StringUtils
 */
public final class StringUtils {
    private StringUtils() {}

    /**
     * Test that specified codePoint is an ASCII letter or digit
     * @param cp codePoint
     * @return true for specified chars
     */
    public static boolean isAz09(int cp) {
        return cp >= '0' && cp <= '9' ||
                cp >= 'a' && cp <= 'z' ||
                cp >= 'A' && cp <= 'Z';
    }

    /**
     * Test that specified codePoint is an ASCII letter, digit or hyphen '-'.
     * @param cp codePoint
     * @return true for specified chars
     */
    public static boolean isAz09Hyp(int cp) {
        return isAz09(cp) || cp == '-';
    }

    /**
     * Test that specified codePoint is an ASCII letter, digit or hyphen '-', '_', ':', '.'. <p/>
     * It common matcher that limit alphabet acceptable for our system IDs.
     * @param cp codePoint
     * @return true for specified chars
     */
    public static boolean isId(int cp) {
        return isAz09(cp) || cp == '-' || cp == '_' || cp == ':' || cp == '.';
    }

    /**
     * Test that specified codePoint is an HEX letter
     * @param cp codePoint
     * @return true for specified chars
     */
    public static boolean isHex(int cp) {
        return cp >= '0' && cp <= '9' ||
                cp >= 'a' && cp <= 'f' ||
                cp >= 'A' && cp <= 'F';
    }

    /**
     * Test that each char of specified string match for predicate. <p/>
     * Note that it method does not support unicode, because it usual applicable only for match letters that placed under 128 code.
     * @param str string
     * @param predicate char matcher
     * @return true if all chars match
     */
    public static boolean match(String str, IntPredicate predicate) {
        final int len = str.length();
        if(len == 0) {
            return false;
        }
        for(int i = 0; i < len; i++) {
            if(!predicate.test(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Is a <code>match(str, StringUtils::isAz09Hyp);</code>.
     * @param str string
     * @return true if string match [A-Za-z0-9-]*
     */
    public static boolean matchAz09Hyp(String str) {
        return match(str, StringUtils::isAz09Hyp);
    }

    /**
     * Is a <code>match(str, StringUtils::isId);</code>.
     * @param str string
     * @return true if string match [A-Za-z0-9-_:.]*
     */
    public static boolean matchId(String str) {
        return match(str, StringUtils::isId);
    }

    /**
     * Is a <code>match(str, StringUtils::isHex);</code>.
     * @param str string
     * @return true if string match [A-Fa-f0-9]*
     */
    public static boolean matchHex(String str) {
        return match(str, StringUtils::isHex);
    }
}
