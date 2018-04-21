package edu.scut.cs.hm.common.utils;

import org.apache.commons.lang3.CharUtils;

import java.util.Random;
import java.util.UUID;

/**
 * Some uuid utils
 */
public final class UUIDs {
    private static final Random RANDOM = new Random();

    private UUIDs() {}

    /**
     * Fast uuid validation. Accept only 36byte HEX with '-' value.
     * @param uuid
     * @throws IllegalArgumentException
     */
    public static void validate(String uuid) throws IllegalArgumentException {
        if (uuid == null) {
            throw new IllegalArgumentException("UUID is null");
        }
        final int length = uuid.length();
        if (length != 36) {
            throw new IllegalArgumentException("UUID: '" + uuid + "' - has invalid length");
        }
        int i = 0;
        while (i < length) {
            char c = uuid.charAt(i);
            if (CharUtils.isAsciiAlpha(c)
                    || CharUtils.isAsciiNumeric(c)
                    || (c == '-' && (i == 8 || i == 13 || i == 18 || i == 23))) {
                i++;
                continue;
            }
            throw new IllegalArgumentException("UUID: '" + uuid + "' - has invalid char '" + c
                    + "' at " + i + ".");
        }
    }

    /**
     * Create lite random uuid (it use non secure pseudo random generator).
     */
    public static UUID liteRandom() {
        long most = RANDOM.nextLong();
        long last = RANDOM.nextLong();
        return new UUID(most, last);
    }

    /**
     * Long based random uid.
     * @return
     */
    public static String longUid() {
        return Long.toUnsignedString(RANDOM.nextLong(), 32);
    }
}
