package edu.scut.cs.hm.common.kv.mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.scut.cs.hm.common.kv.KeyValueStorage;

/**
 * Abstract Mapping a {@link edu.scut.cs.hm.common.kv.KvNode} with string path to a object
 * @param <T>
 */
abstract class AbstractMapping<T> {
    protected final Class<T> type;
    protected final KvMapperFactory mapperFactory;

    AbstractMapping(KvMapperFactory mapperFactory, Class<T> type) {
        this.mapperFactory = mapperFactory;
        this.type = type;
    }

    protected KeyValueStorage getStorage() {
        return this.mapperFactory.getStorage();
    }

    protected ObjectMapper getObjectMapper() {
        return this.mapperFactory.getObjectMapper();
    }

    /**
     * save value in the path in k-v volume, save filed in object one by one
     * and when save one field in object will call 'callback'
     * @param path
     * @param object
     * @param callback
     */
    abstract void save(String path, T object, KvSaveCallback callback);

    /**
     * load value from path
     * @param path
     * @param object
     */
    abstract void load(String path, T object);
    abstract <S extends T> S load(String path, String name, Class<S> type);
}
