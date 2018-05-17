package edu.scut.cs.hm.common.utils;

import lombok.Value;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Map;

/**
 * The immutable key with type for any purposes.
 */
@Value
public final class Key<T> implements Serializable {
    private final Class<T> type;
    private final String name;

    public Key(Class<T> type) {
        this(type.getName(), type);
    }

    public Key(String name, Class<T> type) {
        Assert.notNull(name, "name is nul");
        this.name = name;
        Assert.notNull(type, "type is null");
        this.type = type;
    }

    public static <T> T get(Map<Key<?>, ?> map, Key<T> key) {
        return key.cast(map.get(key));
    }

    public T cast(Object obj) {
        return type.cast(obj);
    }
}
