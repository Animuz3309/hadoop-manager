package edu.scut.cs.hm.common.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class FindHandlerUtil {
    private FindHandlerUtil() {}

    /**
     * This method find appropriate handler by class hierarchy, if got none, then it find by interface hierarchy.
     * @param clazz
     * @param map
     * @param <T>
     * @param <H>
     * @return
     */
    public static <T, H> H findByClass(Class<T> clazz, Map<? extends Class<?>, ? extends H> map) {
        Class<?> c = clazz;
        List<Class<?>> ifaces = new ArrayList<>();
        while(c != null) {
            H handler = map.get(c);
            if(handler != null) {
                return handler;
            }
            ifaces.addAll(Arrays.asList(c.getInterfaces()));
            c = c.getSuperclass();
        }
        //note that ifaces can be duplicated in list
        for(Class<?> iface: ifaces) {
            while(iface != null) {
                H handler = map.get(iface);
                if(handler != null) {
                    return handler;
                }
                iface = iface.getSuperclass();
            }
        }
        return null;
    }
}
