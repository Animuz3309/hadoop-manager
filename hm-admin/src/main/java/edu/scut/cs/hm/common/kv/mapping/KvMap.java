package edu.scut.cs.hm.common.kv.mapping;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * KeyValue storage map of directory. <p/>
 */
@Slf4j
public class KvMap<T> {

    private final KvClassMapper<Object> mapper; // with a specified prefix
    private final KvMapAdapter<T> adapter;
    private final Consumer<KvMapEvent<T>> listener;
    private final Consumer<KvMapLocalEvent<T>> localListener;
    private final Map<String, ValueHolder> map = new LinkedHashMap<>();
    private final boolean passDirty;

    @SuppressWarnings("unchecked")
    private KvMap() {

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

        }
    }

    private ValueHolder getOrCreateHolder(String key) {
        Assert.hasText(key, "key is null or empty");
        synchronized (map) {
            return map.computeIfAbsent(key, ValueHolder::new);
        }
    }

    private void onLocal(KvMapLocalEvent.Action action, ValueHolder holder, T oldValue, T newValue) {
        if(localListener == null) {
            return;
        }
        KvMapLocalEvent<T> event = new KvMapLocalEvent<>(this, holder.key, oldValue, newValue, action);
        localListener.accept(event);
    }

    /**
     * Hold the exact value, as a Cache
     */
    private final class ValueHolder {
        private final String key;   // path name of node in k-v storage
        private volatile T value;
        private final Map<String, Long> index = new ConcurrentHashMap<>();
        private volatile boolean dirty = true;
        private volatile boolean barrier = false;

        ValueHolder(String key) {
            Assert.notNull(key, "key is null");
            this.key = key;
        }

        synchronized T get() {
            if (dirty) {
                load();
            }
            return value;
        }

        synchronized void load() {
            if (barrier) {
                throw new IllegalArgumentException("Recursion detected.");
            }
            // set true to barrier other operation
            barrier = true;
            try {
                T old = (dirty && !passDirty) ? null : value;
                Object obj = mapper.load(key, adapter.getType(old));    // load object from k-v storage
                T newVal = null;
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
    }
}
