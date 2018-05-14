package edu.scut.cs.hm.common.utils;

import edu.scut.cs.hm.model.MethodsProperty;
import edu.scut.cs.hm.common.pojo.Property;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * some utilities
 *
 */
public final class PojoUtils {

    private static final String PREFIX_SET = "set";
    private static final int PREFIX_SET_LENGTH = PREFIX_SET.length();
    private static final String PREFIX_GET = "get";
    private static final int PREFIX_GET_LENGTH = PREFIX_GET.length();
    private static final String PREFIX_IS = "is";
    private static final int PREFIX_IS_LENGTH = PREFIX_IS.length();

    /**
     * unmodifiable map with class properties
     * @param type
     * @return
     */
    public static Map<String, Property> load(Class<?> type) {
        Map<String, MethodsProperty.Builder> map = loadMethodPropertyBuilders(type);

        // build properties
        Map<String, Property> propertyMap  = new TreeMap<>();
        for(MethodsProperty.Builder builder: map.values()) {
            final MethodsProperty property = builder.build();
            propertyMap.put(property.getName(), property);
        }
        return Collections.unmodifiableMap(propertyMap);
    }

    public static Map<String, MethodsProperty.Builder> loadMethodPropertyBuilders(Class<?> type) {
        Method[] methods = type.getMethods();
        Map<String, MethodsProperty.Builder> map = new TreeMap<>();
        for(Method method: methods) {
            final String name = method.getName();
            final String propertyName;
            boolean getter = true;
            if(name.startsWith(PREFIX_GET)) {
                propertyName = decapitalize(name.substring(PREFIX_GET_LENGTH));
            } else if(name.startsWith(PREFIX_IS)) {
                propertyName = decapitalize(name.substring(PREFIX_IS_LENGTH));
            } else if(name.startsWith(PREFIX_SET)) {
                propertyName = decapitalize(name.substring(PREFIX_SET_LENGTH));
                getter = false;
            } else {
                continue;
            }
            if(propertyName.isEmpty()) {
                //it`s maybe if name = "is", "get", "set"
                continue;
            }
            MethodsProperty.Builder property = map.computeIfAbsent(propertyName, k -> MethodsProperty.build(propertyName));
            if(getter) {
                property.setGetter(method);
            } else {
                property.setSetter(method);
            }
        }
        return map;
    }

    /**
     * change first letter to lower case
     * @param string
     * @return
     */
    public static String decapitalize(String string) {
        if(string == null || string.isEmpty()) {
            return string;
        }
        final char chars[] = string.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }
}
