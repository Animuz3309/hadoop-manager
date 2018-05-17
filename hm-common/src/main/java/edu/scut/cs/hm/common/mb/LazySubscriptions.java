package edu.scut.cs.hm.common.mb;

import edu.scut.cs.hm.common.utils.Closeables;
import edu.scut.cs.hm.common.utils.Key;
import lombok.Data;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class LazySubscriptions<M> implements Subscriptions<M>, AutoCloseable {
    public interface Subscriber<M> {
        /**
         *
         * @param consumer
         * @return handler for interrupt event stream
         */
        Runnable subscribe(LazySubscriptions<M>.Context consumer);
    }

    public class Context implements AutoCloseable {

        public void accept(M message) {
            LazySubscriptions.this.accept(message);
        }

        /**
         * Clean but not close current subscription. This not equal with {@link LazySubscriptions#close()}.
         */
        @Override
        public void close() {
            LazySubscriptions.this.clean();
        }
    }

    @Data
    public static class Builder<M> {
        private String id;
        private final Class<M> type;
        private Subscriber<M> subscriber;

        public Builder(Class<M> type) {
            this.type = type;
        }

        public Builder<M> id(String id) {
            setId(id);
            return this;
        }

        public Builder subscriber(Subscriber<M> subscriber) {
            setSubscriber(subscriber);
            return this;
        }

        public LazySubscriptions<M> build() {
            return new LazySubscriptions<>(this);
        }
    }

    private final String id;
    private final Class<M> type;
    private final Subscriber<M> subscriber;
    private final Object busLock = new Object();
    private volatile MessageBus<M> bus;
    private volatile Runnable closer;
    private final AtomicBoolean closed = new AtomicBoolean();

    private LazySubscriptions(Builder<M> builder) {
        this.id = builder.id;
        this.type = builder.type;
        this.subscriber = builder.subscriber;
    }

    public static <M> Builder<M> builder(Class<M> type) {
        return new Builder<M>(type);
    }

    @Override
    public Class<M> getType() {
        return type;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void unsubscribe(Consumer<M> listener) {
        getSubs().unsubscribe(listener);
    }

    @Override
    public Subscription openSubscription(Consumer<M> listener) {
        //additional lock prevent subscription on detached bus
        synchronized (busLock) {
            return getSubs().openSubscription(listener);
        }
    }

    @Override
    public void subscribe(Consumer<M> listener) {
        //additional lock prevent subscription on detached bus
        synchronized (busLock) {
            getSubs().subscribe(listener);
        }
    }

    private Subscriptions<M> getSubs() {
        return getBus().asSubscriptions();
    }

    public MessageBus<M> getBus() {
        if(this.bus == null) {
            synchronized (busLock) {
                if(this.bus == null) {
                    MessageBus<M> bus = MessageBusImpl.builder(type, MessageSubscriptionsWrapper::new)
                            .id(id)
                            .onUnsubscribe(this::onUnsubscribe)
                            .build();
                    this.closer = subscriber.subscribe(new Context());
                    this.bus = bus;
                }
            }
        }
        return this.bus;
    }

    private void onUnsubscribe(MessageBus<M> bus, Consumer<M> consumer) {
        synchronized (busLock) {
            if(this.bus != bus) {
                return;
            }
            if(bus.isEmpty()) {
                clean();
            }
        }
    }

    private void accept(M m) {
        MessageBus<M> bus = this.bus;
        if(bus == null) {
            return;
        }
        bus.accept(m);
    }

    @Override
    public <T> T getOrCreateExtension(Key<T> key, ExtensionFactory<T, M> factory) {
        return getSubs().getOrCreateExtension(key, factory);
    }

    @Override
    public <T> T getExtension(Key<T> key) {
        return getSubs().getExtension(key);
    }

    @Override
    public void close() throws Exception {
        if(!this.closed.compareAndSet(false, true)) {
            return;
        }
        clean();
    }

    /**
     * Due this subscription "lazy", it close subscription each time when no listeners.
     * Therefore we can call {@link #clean()} and {@link #getBus()} many time at lifecycle.
     */
    private void clean() {
        Runnable closer;
        MessageBus<M> bus;
        synchronized (busLock) {
            bus = this.bus;
            this.bus = null;
            closer = this.closer;
        }
        Closeables.close(bus);
        if(closer != null) {
            closer.run();
        }
    }
}
