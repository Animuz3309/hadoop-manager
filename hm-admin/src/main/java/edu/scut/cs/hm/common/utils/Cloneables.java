package edu.scut.cs.hm.common.utils;

import org.springframework.beans.BeanUtils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

public final class Cloneables {
    private Cloneables() {
    }

    /**
     * @param src
     * @param <T>
     */
    @SuppressWarnings("unchecked")
    public static <T> T clone(T src) {
        if(src == null) {
            return null;
        }
        Class<?> clazz = src.getClass();
        if(Map.class.isAssignableFrom(clazz)) {
            return (T) clone((Map<?, ?>) src);
        }
        if(Collection.class.isAssignableFrom(clazz)) {
            return (T) clone((Collection<?>) src);
        }
        if(Cloneable.class.isAssignableFrom(clazz)) {
            try {
                Method cloner = clazz.getDeclaredMethod("clone");
                return (T) cloner.invoke(src);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Can not clone instance of " + clazz, e);
            }
        }
        return src;
    }

    @SuppressWarnings({"unchecked", "deprecation"})
    public static <K, V> Map<K, V> clone(Map<K, V> src) {
        Map<K, V> map = BeanUtils.instantiate(src.getClass());
        src.forEach((k, v) -> map.put(clone(k), clone(v)));
        return map;
    }

    @SuppressWarnings({"unchecked", "deprecation"})
    private static <T> Collection<T> clone(Collection<T> src) {
        // also we may preallocate lists for src size
        Collection<T> collection = BeanUtils.instantiate(src.getClass());
        src.forEach(i -> collection.add(clone(i)));
        return src;
    }
}
