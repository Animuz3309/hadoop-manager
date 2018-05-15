package edu.scut.cs.hm.common.mb;

import edu.scut.cs.hm.common.utils.Closeables;

import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ConditionalMessageBusWrapper<M, K> implements ConditionalSubscriptions<M, K> {
    /**
     * A condition consumer only when the message satisfy the key
     * The {@link Function} keyExtractor decided the current key in message
     */
    final class ConditionalConsumer implements Consumer<M>, WrappedConsumer<M> {

        private final K key;
        private final Consumer<M> consumer;

        public ConditionalConsumer(K key, Consumer<M> consumer) {
            this.key = key;
            this.consumer = consumer;
        }

        @Override
        public void accept(M m) {
            K currentKey = keyExtractor.apply(m);
            if(!predicate.test(key, currentKey)) {
                return;
            }
            this.consumer.accept(m);
        }

        @Override
        public Consumer<M> unwrap() {
            return consumer;
        }

        @Override
        public void close() throws Exception {
            Closeables.closeIfCloseable(consumer);
        }
    }

    private final Subscriptions<M> subscriptions;
    private final Function<M, K> keyExtractor;
    private final BiPredicate<K, K> predicate;

    public ConditionalMessageBusWrapper(Subscriptions<M> subscriptions,
                                        Function<M, K> keyExtractor,
                                        BiPredicate<K, K> predicate) {
        this.subscriptions = subscriptions;
        this.keyExtractor = keyExtractor;
        this.predicate = predicate;
    }

    @Override
    public void subscribeOnKey(Consumer<M> listener, K key) {
        ConditionalConsumer cc = new ConditionalConsumer(key, listener);
        this.subscriptions.subscribe(cc);
    }

    @Override
    public void subscribe(Consumer<M> listener) {
        subscriptions.subscribe(listener);
    }

    @Override
    public Subscription openSubscription(Consumer<M> listener) {
        return subscriptions.openSubscription(listener);
    }

    @Override
    public void unsubscribe(Consumer<M> listener) {
        subscriptions.unsubscribe(listener);
    }

    @Override
    public String getId() {
        return subscriptions.getId();
    }

    @Override
    public Class<M> getType() {
        return subscriptions.getType();
    }
}
