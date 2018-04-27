package edu.scut.cs.hm.common.mb;

import java.util.function.Consumer;

/**
 * Extension of {@link Subscriptions} that support subscribe listener on a specified key
 * @param <M>
 * @param <K>
 */
public interface ConditionalSubscriptions<M, K> extends Subscriptions<M> {

    /**
     * Subscribe on specified keys
     * @param listener
     * @param key event key, what part of event is key depend from key extractor function
     */
    void subscribeOnKey(Consumer<M> listener, K key);

    /**
     * Act like {@link #subscribe(Consumer)}, but return closeable {@link Subscription} which can do unsubscribe in its
     * @param listener
     * @param key event key, what part of event is key depend from key extractor function
     * @return
     */
    default Subscription openSubscriptionOnKey(Consumer<M> listener, K key) {
        subscribe(listener);
        return new SubscriptionImpl<>(this, listener);
    }
}
