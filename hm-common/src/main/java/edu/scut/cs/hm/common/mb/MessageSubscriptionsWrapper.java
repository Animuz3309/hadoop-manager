package edu.scut.cs.hm.common.mb;

import edu.scut.cs.hm.common.utils.Key;

import java.util.function.Consumer;

/**
 * Simple wrapper which is hide message bus method like {@link MessageBus#accept(Object)} from interface users.
 * @param <M>
 */
public class MessageSubscriptionsWrapper<M> implements Subscriptions<M> {
    private final Subscriptions<M> orig;

    public MessageSubscriptionsWrapper(Subscriptions<M> orig) {
        this.orig = orig;
    }

    @Override
    public void subscribe(Consumer<M> listener) {
        orig.subscribe(listener);
    }

    @Override
    public Subscription openSubscription(Consumer<M> listener) {
        return orig.openSubscription(listener);
    }

    @Override
    public void unsubscribe(Consumer<M> listener) {
        orig.unsubscribe(listener);
    }

    @Override
    public String getId() {
        return orig.getId();
    }

    @Override
    public Class<M> getType() {
        return orig.getType();
    }

    @Override
    public <T> T getExtension(Key<T> key) {
        return orig.getExtension(key);
    }

    @Override
    public <T> T getOrCreateExtension(Key<T> key, ExtensionFactory<T, M> factory) {
        return orig.getOrCreateExtension(key, factory);
    }
}
