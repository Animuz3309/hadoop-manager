package edu.scut.cs.hm.common.kv.mapping;

/**
 * do something with KvProperty before save and read
 */
public interface PropertyInterceptor {

    /**
     * invoked at save property to storage
     * @param value
     * @return
     */
    String save(KvPropertyContext prop, String value);

    /**
     * invoked at read property from storage
     * @param value
     * @return
     */
    String read(KvPropertyContext prop, String value);
}
