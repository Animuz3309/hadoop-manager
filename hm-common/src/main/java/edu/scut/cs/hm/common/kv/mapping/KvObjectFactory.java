package edu.scut.cs.hm.common.kv.mapping;

/**
 * Create Object
 */
public interface KvObjectFactory<T> {

    /**
     * Create object
     */
    T create(String key, Class<? extends T> type);
}
