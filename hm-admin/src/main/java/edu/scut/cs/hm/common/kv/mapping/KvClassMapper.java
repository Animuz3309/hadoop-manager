package edu.scut.cs.hm.common.kv.mapping;

import com.google.common.base.MoreObjects;
import edu.scut.cs.hm.common.kv.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

/**
 * According to Class type to load/save actual java object store in KeyValueStorage
 * indeed use {@link AbstractMapping} to load/save value in KeyValueStorage
 * @param <T> the object class type
 */
@Slf4j
public class KvClassMapper<T> {

    private static final KvObjectFactory<Object> FACTORY = (String key, Class<?> type) -> BeanUtils.instantiateClass(type);

    @Data
    public static class Builder<T> {
        private final KvMapperFactory mapperFactory;
        private final Class<T> type;
        private String prefix;
        private KvObjectFactory<T> factory; // factory to create specified type object

        Builder(KvMapperFactory mapperFactory, Class<T> type) {
            this.mapperFactory = mapperFactory;
            this.type = type;
        }

        public Builder<T> prefix(String prefix) {
            setPrefix(prefix);
            return this;
        }

        public Builder<T> factory(KvObjectFactory<T> factory) {
            setFactory(factory);
            return this;
        }

        public KvClassMapper<T> build() {
            return new KvClassMapper<>(this);
        }
    }

    private final Class<T> type;
    private final String prefix;
    private final KvMapperFactory mapperFactory;       // get specified mapping like NodeMapping or LeafMapping
    private final KeyValueStorage storage;
    private final AbstractMapping<T> mapping;          // the specified mapping get from 'mapperFactory'

    public static <T> Builder<T> builder(KvMapperFactory mf, Class<T> type) {
        return new Builder<>(mf, type);
    }

    @SuppressWarnings("unchecked")
    KvClassMapper(Builder<T> builder) {
        this.mapperFactory = builder.mapperFactory;
        this.prefix = builder.prefix;
        this.type = builder.type;
        this.mapping = this.mapperFactory.getMapping(type,
                MoreObjects.firstNonNull(builder.factory, (KvObjectFactory<T>) FACTORY));
        this.storage = mapperFactory.getStorage();
    }

    public Class<T> getType() {
        return type;
    }

    public String getPrefix() {
        return prefix;
    }

    public void delete(String name) {
        this.storage.delDir(path(name), DeleteDirOptions.builder().recursive(true).build());
    }

    private String path(String name) {
        return KvUtils.join(this.prefix, name);
    }

    /**
     * Save object to storage and return object that allow check for object modifications.
     * @param name the name of path
     * @param object
     * @return
     */
    public void save(String name, T object) {
        save(name, object, null);
    }

    void save(String name, T object, KvSaveCallback callback) {
        this.type.cast(object);
        String path = path(name);
        this.mapping.save(path, object, callback);
    }

    /**
     * list same objects in storage
     * if {@link #prefix} doesn't exist then create new
     * @return
     */
    public List<String> list() {
        List<String> list = this.storage.list(prefix);
        if (list == null) {
            createPrefix();
            list = this.storage.list(prefix);
        }
        return list.stream().map(this::getName).collect(Collectors.toList());
    }

    /**
     * Get relative name from full path.
     * @param path
     * @return
     */
    public String getName(String path) {
        return KvUtils.name(prefix, path);
    }

    private void createPrefix() {
        this.storage.setDir(prefix, WriteOptions.builder().failIfExists(true).build());
    }
    /**
     * Load object from specified node. Name of node can be obtained from {@link #list()}.
     * @param name name of node
     * @return object or null
     */
    public T load(String name) {
        return load(name, null);
    }

    /**
     * Load object from specified node. Name of node can be obtained from {@link #list()}.
     * @param name name of node
     * @param type null or instantiable type, must be a subtype of {@link T}
     * @return object or null
     */
    public <S extends T> S load(String name, Class<S> type) {
        String path = path(name);
        //check that mapped dir is exists
        KvNode node = this.storage.get(path);
        if(node == null) {
            return null;
        }
        Class<S> actualType = resolveType(type);
        return this.mapping.load(path, name, actualType);
    }

    @SuppressWarnings("unchecked")
    private <S extends T> Class<S> resolveType(Class<S> subType) {
        Class<S> actualType = (Class<S>) this.type;
        if(subType != null) {
            Assert.isTrue(this.type.isAssignableFrom(subType), "Specified type " + subType + " must be an subtype of " + this.type);
            actualType = subType;
        }
        return actualType;
    }
}
