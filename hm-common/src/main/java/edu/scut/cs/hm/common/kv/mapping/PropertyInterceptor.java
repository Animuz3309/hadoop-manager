package edu.scut.cs.hm.common.kv.mapping;

/**
 * do something with {@link KvMapping} annotation field before save and read from k-v volume
 * @see KvProperty#get(Object)
 * @see KvProperty#set(Object, String)
 */
public interface PropertyInterceptor {

    /**
     * invoked at save property to volume
     * @param value
     * @return
     */
    String save(KvPropertyContext prop, String value);

    /**
     * invoked at read property from volume
     * @param value
     * @return
     */
    String read(KvPropertyContext prop, String value);
}
