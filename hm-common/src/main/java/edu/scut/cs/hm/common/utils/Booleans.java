package edu.scut.cs.hm.common.utils;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public final class Booleans {

    private static final Map<String, Boolean> MAP;

    static{
        Map<String, Boolean> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        map.put("true", true);
        map.put("ok", true);
        map.put("yes", true);
        map.put("on", true);
        map.put("1", true);

        map.put("false", false);
        map.put("no", false);
        map.put("off", false);
        map.put("0", false);
        MAP = Collections.unmodifiableMap(map);
    }

    private Booleans() {
    }

    public static boolean parse(String s) {
        if(s == null) {
            return false;
        }
        Boolean res = MAP.get(s);
        return res != null && res;
    }

    public static boolean valueOf(Object o) {
        return o != null && (
                o instanceof Boolean && (Boolean)o ||
                        o instanceof String && parse((String)o) ||
                        o instanceof Number && ((Number)o).intValue() != 0
        );
    }
}
