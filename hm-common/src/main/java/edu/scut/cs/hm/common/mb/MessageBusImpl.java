package edu.scut.cs.hm.common.mb;

import edu.scut.cs.hm.common.utils.Closeables;
import edu.scut.cs.hm.common.utils.Key;
import lombok.Data;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Implementation of {@link MessageBus}
 * @param <M> message type
 * @param <S> subclass of {@link Subscriptions}
 */
public final class MessageBusImpl<M, S extends Subscriptions<M>> implements MessageBus<M> {

    @Data
    public static class Builder<M, S extends Subscriptions<M>> {
        private final Class<M> type;
        private final Function<Subscriptions<M>, S> subscriptionsFactory;

        // consumer handle exception happen in message consumer
        protected Consumer<ExceptionInfo> exceptionInfoConsumer = ExceptionInfoLogger.getInstance();
        protected String id;
        // listener on subscribe event
        protected SubscribeListener<M> onSubscribe;
        // listener on unsubscribe event
        protected SubscribeListener<M> onUnsubscribe;

        Builder(Class<M> type, Function<Subscriptions<M>, S> subscriptionsFactory) {
            this.type = type;
            this.subscriptionsFactory = subscriptionsFactory;
        }

        public Builder<M, S> exceptionInfoConsumer(Consumer<ExceptionInfo> exceptionInfoConsumer) {
            setExceptionInfoConsumer(exceptionInfoConsumer);
            return this;
        }

        public Builder<M, S> id(String id) {
            setId(id);
            return this;
        }

        public Builder<M, S> onUnsubscribe(SubscribeListener<M> onUnsubscribe) {
            setOnUnsubscribe(onUnsubscribe);
            return this;
        }

        public Builder<M, S> onSubscribe(SubscribeListener<M> onSubscribe) {
            setOnSubscribe(onSubscribe);
            return this;
        }

        public MessageBusImpl<M, S> build() {
            return new MessageBusImpl<>(this);
        }

    }

    private final AtomicReference<List<Consumer<M>>> listenersRef = new AtomicReference<>(Collections.emptyList());
    private final String id;
    private final Class<M> type;
    private final Consumer<ExceptionInfo> exceptionInfoConsumer;
    private final S subscriptions;
    private final SubscribeListener<M> onSubscribe;
    private final SubscribeListener<M> onUnsubscribe;
    private final ConcurrentMap<Key<?>, Object> extensions = new ConcurrentHashMap<>();

    private MessageBusImpl(Builder<M, S> b) {
        Assert.hasText(b.id, "id is null or empty");
        this.id = b.id;
        Assert.notNull(b.type, "type is null");
        this.type = b.type;
        Assert.notNull(b.exceptionInfoConsumer, "exceptionInfoConsumer is null");
        this.exceptionInfoConsumer = b.exceptionInfoConsumer;
        Assert.notNull(b.subscriptionsFactory, "subscriptionsFactory is null");
        // this implements 'MessageBus' supported 'Subscriptions'
        this.subscriptions = b.subscriptionsFactory.apply(this);
        this.onSubscribe = b.onSubscribe;
        this.onUnsubscribe = b.onUnsubscribe;
    }

    /**
     * Return a MessageBus Builder
     * @param messageType   type of message
     * @param subscriptionFactory input a Subscriptions return maybe a wrapped Subscriptions like {@link ConditionalSubscriptions}
     * @param <M> type of message
     * @param <S> a new Subscriptions
     * @return
     */
    public static <M, S extends Subscriptions<M>> Builder<M, S>
                builder(Class<M> messageType, Function<Subscriptions<M>, S> subscriptionFactory) {
        return new Builder<>(messageType, subscriptionFactory);
    }


    @Override
    public String getId() {
        return id;
    }

    @Override
    public Class<M> getType() {
        return type;
    }


    @Override
    public void accept(M message) {
        // first we must check message for correct type
        type.cast(message);

        final List<Consumer<M>> list = listenersRef.get();
        for (Consumer<M> consumer: list) {
            // catch exception info caused by consumer
            invoke(consumer, message);
        }
    }

    private void invoke(Consumer<M> consumer, M message) {
        try {
            consumer.accept(message);
        } catch (Exception e) {
            ExceptionInfo ei = new ExceptionInfo(this, e, consumer, message);
            exceptionInfoConsumer.accept(ei);
        }
    }

    @Override
    public void subscribe(Consumer<M> listener) {
        Assert.notNull(listener, "listener is null");
        // cas operation (sync listeners list)
        while (true) {
            final List<Consumer<M>> srcList = listenersRef.get();
            if (contains(srcList, listener)) {
                return;
            }
            List<Consumer<M>> tmp = new ArrayList<>(srcList.size() + 1);
            tmp.addAll(srcList);
            tmp.add(listener);
            List<Consumer<M>> dstList = Collections.unmodifiableList(tmp);
            if (listenersRef.compareAndSet(srcList, dstList)) {
                if (onSubscribe != null) {
                    // notify onSubscribe listener a subscribe event happened
                    onSubscribe.event(this, listener);
                }
                return;
            }
        }
    }

    @Override
    public void unsubscribe(Consumer<M> listener) {
        Assert.notNull(listener, "listener is null");
        while (true) {
            final List<Consumer<M>> srcList = listenersRef.get();
            int i = indexOf(srcList, listener);
            if(i < 0) {
                return;
            }
            List<Consumer<M>> tmp = new ArrayList<>(srcList);
            tmp.remove(i);
            List<Consumer<M>> dstList = Collections.unmodifiableList(tmp);
            if(listenersRef.compareAndSet(srcList, dstList)) {
                if(onUnsubscribe != null) {
                    // notify onUnsubscribe listener a unsubscribe event happened
                    onUnsubscribe.event(this, listener);
                }
                return;
            }
        }
    }

    private boolean contains(List<Consumer<M>> srcList, Consumer<M> key) {
        return indexOf(srcList, key) >= 0;
    }

    private int indexOf(List<Consumer<M>> list, Consumer<M> key) {
        Consumer<M> unwrappedKey = unwrap(key);
        int size = list.size();
        for(int i = 0; i < size; i++) {
            Consumer<M> element = unwrap(list.get(i));
            if(element == unwrappedKey) {
                return i;
            }
        }
        return -1;
    }

    private Consumer<M> unwrap(Consumer<M> key) {
        return WrappedConsumer.unwrap(key);
    }

    @Override
    public boolean isEmpty() {
        List<Consumer<M>> consumers = listenersRef.get();
        return consumers.isEmpty();
    }

    @Override
    public Subscriptions<M> asSubscriptions() {
        return subscriptions;
    }

    @Override
    public <T> T getOrCreateExtension(Key<T> key, ExtensionFactory<T, M> factory) {
        Assert.notNull(key, "key is null");
        Object old = this.extensions.computeIfAbsent(key, (k) -> factory.create(key, this));
        return key.cast(old);
    }

    @Override
    public <T> T getExtension(Key<T> key) {
        Assert.notNull(key, "key is null");
        return Key.get(this.extensions, key);
    }

    @Override
    public void close() throws Exception {
        List<Consumer<M>> old = listenersRef.getAndSet(Collections.emptyList());
        old.forEach(Closeables::closeIfCloseable);
        this.extensions.values().forEach(Closeables::closeIfCloseable);
        this.extensions.clear();
    }
}
