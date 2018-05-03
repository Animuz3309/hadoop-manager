package edu.scut.cs.hm.common.kv.mapping;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import edu.scut.cs.hm.common.kv.KvStorageEvent;
import edu.scut.cs.hm.common.kv.KvUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * KeyValue storage map of directory. <p/>
 * The local cached storage of value in remote Key-Value storage
 */
@Slf4j
public class KvMap<T> {

    @Data
    public static class Builder<T, V> {
        private KvMapperFactory mapperFactory;  // factory to get local obj vs remote node in k-v storage mapping
        private String path;                // the node path in remote k-v storage
        private KvMapAdapter<T> adapter = KvMapAdapter.direct();    // the adapter to get value from k-v node
        private final Class<T> type;        // type of obj
        private final Class<V> valueType;   // type of obj
        /**
         * Note that it invoke at event caused by map user. For event from KV storage use {@link #setListener(Consumer)}.
         */
        private Consumer<KvMapLocalEvent<T>> localListener;
        /**
         * Note that it handle event from KV storage. For event caused by map user use {@link #setLocalListener(Consumer)} .
         */
        private Consumer<KvMapEvent<T>> listener;
        private KvObjectFactory<V> factory;
        /**
         * Pass dirty value into adapter. Otherwise adapter receive null value.
         * <p/>
         * Default - false;
         */
        private boolean passDirty;

        public Builder(Class<T> type, Class<V> valueType) {
            Assert.notNull(type, "type is null");
            this.type = type;
            this.valueType = valueType;
        }

        public Builder<T, V> mapper(KvMapperFactory factory) {
            setMapperFactory(factory);
            return this;
        }

        public Builder<T, V> path(String path) {
            setPath(path);
            return this;
        }

        public Builder<T, V> adapter(KvMapAdapter<T> adapter) {
            setAdapter(adapter);
            return this;
        }

        /**
         * Note that it invoke at event caused by map user. For event from KV storage use {@link #setListener(Consumer)}.
         * @param consumer handler for local event causet by invoking of map methods
         * @return this
         */
        public Builder<T, V> localListener(Consumer<KvMapLocalEvent<T>> consumer) {
            setLocalListener(consumer);
            return this;
        }

        /**
         * Note that it handle event from KV storage. For event caused by map user use {@link #setLocalListener(Consumer)} .
         * @param listener handler for KV storage event.
         * @return this
         */
        public Builder<T, V> listener(Consumer<KvMapEvent<T>> listener) {
            setListener(listener);
            return this;
        }

        public Builder<T, V> factory(KvObjectFactory<V> factory) {
            setFactory(factory);
            return this;
        }

        /**
         * Pass dirty value into adapter. Otherwise adapter receive null value.
         * <p/>
         * Default - false;
         * @param passDirty flag
         * @return this
         */
        public Builder<T, V> passDirty(boolean passDirty) {
            setPassDirty(passDirty);
            return this;
        }

        public KvMap<T> build() {
            Assert.notNull(type, "type can't be null");
            return new KvMap<>(this);
        }
    }

    /**
     * Used for replace this property in ValueHolder index map
     */
    static final String THIS = " this";
    private final KvClassMapper<Object> mapper;                             // crud obj with specified mapping
    private final KvMapAdapter<T> adapter;                                  // set and get value
    private final Consumer<KvMapEvent<T>> listener;                         // listen remote k-v storage event
    private final Consumer<KvMapLocalEvent<T>> localListener;               // listen local kvmap event
    private final Map<String, ValueHolder> map = new LinkedHashMap<>();     // hold the value
    private final boolean passDirty;                                        // whether pass dirty value

    public static <T> Builder<T, T> builder(Class<T> type) {
        return new Builder<>(type, type);
    }

    public static <T, V> Builder<T, V> builder(Class<T> type, Class<V> value) {
        return new Builder<>(type, value);
    }

    @SuppressWarnings("unchecked")
    private KvMap(Builder builder) {
        Assert.notNull(builder.mapperFactory, "mapperFactory is null");
        Assert.notNull(builder.path, "path is null");
        this.adapter = builder.adapter;
        this.localListener = builder.localListener;
        this.listener = builder.listener;
        this.passDirty = builder.passDirty;
        Assert.isTrue(!this.passDirty || this.adapter != KvMapAdapter.DIRECT, "Direct adapter does not support passDirty flag.");

        // the object type need to map
        Class<Object> mapperType = MoreObjects.firstNonNull(builder.valueType, (Class<Object>)builder.type);

        // object mapperFactory (map java obj to k-v storage)
        this.mapper = builder.mapperFactory.buildClassMapper(mapperType)
                .prefix(builder.path)       // prefix in k-v storage
                .factory(builder.factory)
                .build();

        // subscribe KvEvent Listener with specified key(prefix aka path) on k-v storage
        // we can see this on EtcdClientWrapper#eventWhirligig(long)
        // MessageBus.accept() will invoke this listener
        builder.mapperFactory.getStorage().subscriptions().subscribeOnKey(this::onKvEvent, builder.path);

    }

    // the listener subscribe on this.mapperFactory.getStorage() to listen KvStorageEvent
    // we can see this on EtcdClientWrapper#eventWhirligig(long)
    // MessageBus.accept() will invoke this listener
    private void onKvEvent(KvStorageEvent e) {
        final long index = e.getIndex();                // the index that the identify the obj in k-v storage is primary key and unique
        String path = e.getKey();                       // the full obj path in k-v storage
        String key = this.mapper.getName(path);         // get relative name from full path.
        KvStorageEvent.Crud action = e.getAction();     // the even action

        if (key == null) {
            // it means that someone remove mapped node with all entries, we must clear map
            if (action == KvStorageEvent.Crud.DELETE) {
                List<ValueHolder> set;
                synchronized (map) {
                    set = new ArrayList<>(map.values());
                    map.clear();
                }
                set.forEach(valueHolder -> {
                    onLocal(KvMapLocalEvent.Action.DELETE, valueHolder, valueHolder.getIfPresent(), null);
                    invokeListener(KvStorageEvent.Crud.DELETE, valueHolder.key, valueHolder);
                });
            }
            return;
        }

        String property = KvUtils.child(this.mapper.getPrefix(), path, 1);  // child node
        ValueHolder holder = null;

        if(property != null) {
            // get the child node's mapped value
            holder = getOrCreateHolder(key);
            // dirty local value if remote node value is modified
            holder.dirty(property, index);
        } else {
            switch (action) {
                case CREATE:
                    // we use lazy loading - just create key but not load value
                    holder = getOrCreateHolder(key);
                    break;
                case READ:
                    //ignore
                    break;
                case UPDATE:
                    holder = getOrCreateHolder(key);
                    holder.dirty(null, index);
                    break;
                case DELETE:
                    synchronized (map) {
                        holder = map.remove(key);
                        if(holder != null) {
                            // the delete event must be handle in local event listener 'localListener'
                            onLocal(KvMapLocalEvent.Action.DELETE, holder, holder.getIfPresent(), null);
                        }
                    }
            }
        }

        invokeListener(action, key, holder);
    }

    /**
     * invoke {@link #listener}
     * @param action
     * @param key
     * @param holder
     */
    private void invokeListener(KvStorageEvent.Crud action, String key, ValueHolder holder) {
        if(listener != null) {
            T value = null;
            if(holder != null) {
                // we cah obtain value before it become dirty, but it will wrong behavior
                value = holder.getIfPresent();
            }
            listener.accept(new KvMapEvent<>(this, key, value, action));
        }
    }

    /**
     * invoke {@link #localListener}
     * @param action
     * @param holder
     * @param oldValue
     * @param newValue
     */
    private void onLocal(KvMapLocalEvent.Action action, ValueHolder holder, T oldValue, T newValue) {
        if(localListener == null) {
            return;
        }
        KvMapLocalEvent<T> event = new KvMapLocalEvent<>(this, holder.key, oldValue, newValue, action);
        localListener.accept(event);
    }

    /**
     * Load all data
     */
    public void load() {
        this.mapper.list().forEach(this::getOrCreateHolder);
    }

    public T get(String key) {
        ValueHolder holder = getOrCreateHolder(key);
        T val = holder.get();
        if (val == null) {
            synchronized (map) {
                map.remove(key, holder);
            }
            return null;
        }
        return val;
    }

    /**
     * Get exists value from storage. Not load it, even if dirty.
     * @param key key
     * @return value or null if not exists or dirty.
     */
    public T getIfPresent(String key) {
        ValueHolder holder;
        synchronized (map) {
            holder = map.get(key);
        }
        if (holder == null) {
            return null;
        }
        return holder.getIfPresent();
    }

    /**
     * Put and save value into storage.
     * @param key key
     * @param val value
     * @return old value if exists
     */
    public T put(String key, T val) {
        ValueHolder holder = getOrCreateHolder(key);
        return holder.save(val);
    }

    /**
     * Remove value directly from storage, from map it will be removed at event.
     *
     * @param key key for remove
     * @return gives value only if present, not load it, this mean that you may obtain null, event storage has value
     */
    public T remove(String key) {
        ValueHolder valueHolder;
        synchronized (map) {
            valueHolder = map.get(key);
            // we not delete holder here, it must be delete from kv-event listener
        }
        mapper.delete(key);
        if (valueHolder != null) {
            // we must not load value
            return valueHolder.getIfPresent();
        }
        return null;
    }

    /**
     * Compute a value if the value of key is not exists
     * @param key
     * @param func
     * @return
     */
    public T computeIfAbsent(String key, Function<String, ? extends T> func) {
        ValueHolder holder = getOrCreateHolder(key);
        return  holder.computeIfAbsent(func);
    }


    /**
     * Invoke on key. If ValueHolder exists then passed to fun, otherwise null. If func return null value will be removed.
     * @param key key
     * @param func handler
     * @return new value
     */
    public T compute(String key, BiFunction<String, ? super T, ? extends T> func) {
        ValueHolder holder = getOrCreateHolder(key);
        T newVal = holder.compute(func);
        if(newVal == null) {
            synchronized (map) {
                map.remove(key, holder);
            }
        }
        return newVal;
    }

    /**
     * Save existed value of specified key. If is not present, do nothing.
     * @param key key of value.
     */
    public void flush(String key) {
        ValueHolder holder;
        synchronized (map) {
            holder = map.get(key);
        }
        if(holder != null) {
            holder.flush();
        }
    }

    private ValueHolder getOrCreateHolder(String key) {
        Assert.hasText(key, "key is null or empty");
        synchronized (map) {
            return map.computeIfAbsent(key, ValueHolder::new);
        }
    }

    /**
     * Gives Immutable set of keys. Note that it not load keys from storage. <p/>
     * For load keys you need to use {@link #load()}.
     * @return set of keys, never null.
     */
    public Set<String> list() {
        synchronized (map) {
            return ImmutableSet.copyOf(this.map.keySet());
        }
    }

    /**
     * Load all values of map. Note that it may cause time consumption.
     * @return immutable collection of values
     */
    public Collection<T> values() {
        ImmutableList.Builder<T> b = ImmutableList.builder();
        synchronized (map) {
            this.map.values().forEach(valueHolder -> {
                T element = safeGet(valueHolder);
                // map does not contain holders with null elements, but sometime it happen
                // due to multithread access , for example in `put()` method
                if(element != null) {
                    b.add(element);
                }
            });
        }
        return b.build();
    }

    public void forEach(BiConsumer<String, ? super T> action) {
        // we use copy for prevent call external code in lock
        Map<String, ValueHolder> copy;
        synchronized (map) {
            copy = new LinkedHashMap<>(this.map);
        }
        copy.forEach((key, holder) -> {
            T value = safeGet(holder);
            if(value != null) {
                action.accept(key, value);
            }
        });
    }

    private T safeGet(ValueHolder valueHolder) {
        T element = null;
        try {
            element = valueHolder.get();
        } catch (Exception e) {
            log.error("Can not load {}", valueHolder.key, e);
        }
        return element;
    }

    /**
     * Hold the exact value, as a Cache
     */
    private final class ValueHolder {
        private final String key;   // path name of node in k-v storage
        private volatile T value;
        // keep the node's modified index value in k-v storage
        // we can compare the old index value and new value from node in k-v storage to judge whether node's value modified
        // if modified we need to dirty this.value
        // see #dirty(String prop, long newIndex)
        private final Map<String, Long> index = new ConcurrentHashMap<>();
        private volatile boolean dirty = true;
        private volatile boolean barrier = false;

        ValueHolder(String key) {
            Assert.notNull(key, "key is null");
            this.key = key;
        }

        /**
         * save in the this.value and flush k-v stirage
         * @param val
         * @return
         */
        T save(T val) {
            T old;
            KvMapLocalEvent.Action action;
            synchronized (this) {
                checkValue(val);
                // we must not publish dirty value;
                old = getIfPresent();
                this.dirty = false;
                if (val == old) {
                    return old;
                }
                action = old == null ? KvMapLocalEvent.Action.CREATE : KvMapLocalEvent.Action.UPDATE;
                this.value = val;
            }
            onLocal(action, this, old, val);
            // flush value in the k-v storage
            flush();
            return old;
        }

        private void checkValue(T value) {
            Assert.notNull(value, "Null value is not allowed");
        }

        /**
         * save in the k-v storage
         */
        void flush() {
            Object obj;
            synchronized (this) {
                if (this.value == null) {
                    // value not set, nothing to flush
                    return;
                }
                this.dirty = false;
                obj = adapter.get(this.key, this.value);
            }
            // Note that message will be concatenated with type of object by 'Assert.isInstanceOf'
            Assert.isInstanceOf(mapper.getType(), obj, "Adapter " + adapter + " return object of inappropriate");
            Assert.notNull(obj, "Adapter " + adapter + " return null from " + this.value + " that is not allowed");
            mapper.save(key, obj, (name, res) -> {
                synchronized (this) {
                    // the name is prop name (prop name in 'obj') if is null -> means this
                    // res is KvNode which record the prop value
                    // e.g. /test/entries/one/text - "3upn2g9jjquh2"
                    index.put(toIndexKey(name), res.getIndex());
                }
            });
        }

        private String toIndexKey(String name) {
            return name == null? THIS : name;
        }

        /**
         * newIndex is the modifiedIndex of node in k-v storage
         * if oldIndex != null && oldIndex != newIndex -> value is dirty in local
         * @param prop
         * @param newIndex
         */
        synchronized void dirty(String prop, long newIndex) {
            Long old = this.index.get(toIndexKey(prop));
            if(old != null && old != newIndex) {
                dirty();
            }
        }

        synchronized void dirty() {
            this.dirty = true;
        }

        /**
         * load from k-v storage
         */
        synchronized void load() {
            if (barrier) {
                throw new IllegalArgumentException("Recursion detected.");
            }
            // set true to barrier other operation
            barrier = true;
            try {
                T old = (dirty && !passDirty) ? null : value;
                Object obj = mapper.load(key, adapter.getType(old));    // load object from k-v storage
                T newVal;
                if (obj != null || old != null) {
                    // set load object(obj) to source(old)
                    newVal = adapter.set(this.key, old, obj);
                    // adapter.set is not allowed to return null
                    if (newVal == null) {
                        throw new IllegalArgumentException("Adapter " + adapter +
                                " broke contract: it return null value for non null object.");
                    }
                    this.dirty = false;
                    // here we must raise local event, but need to use another action like LOAD or SET,
                    // UPDATE and CREATE - is not acceptable here
                    this.value = newVal;
                    // submit a local event - LOAD
                    onLocal(KvMapLocalEvent.Action.LOAD, this, old, newVal);
                }
            } finally {
                barrier = false;
            }
        }

        /**
         * load value if its present, but dirty
         */
        synchronized T get() {
            if (dirty) {
                // if dirty load obj from k-v storage
                load();
            }
            return value;
        }

        /**
         * if dirty return null , otherwise load value from k-v storage
         */
        synchronized T getIfPresent() {
            if (dirty) {
                // returning dirty value ma cause unexpected effects
                return null;
            }
            return value;
        }

        synchronized T computeIfAbsent(Function<String, ? extends T> func) {
            get(); // we must try to load before
            if (value == null) {
                save(func.apply(key));
            }
            // get - load value of its present, but dirty
            return get();
        }

        synchronized T compute(BiFunction<String, ? super T, ? extends T> func) {
            get(); // we must try to load before compute
            T newVal = func.apply(key, value);
            if(newVal != null) {
                save(newVal);
            } else {
                return null;
            }
            // get - load value if its present, but dirty
            return get();
        }
    }
}
