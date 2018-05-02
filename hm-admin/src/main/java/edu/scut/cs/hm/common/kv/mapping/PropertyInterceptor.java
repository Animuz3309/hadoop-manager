package edu.scut.cs.hm.common.kv.mapping;

/**
 * do something with {@link KvMapping} annotation field before save and read from k-v storage
 * @see KvProperty#get(Object)
 * @see KvProperty#set(Object, String)
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