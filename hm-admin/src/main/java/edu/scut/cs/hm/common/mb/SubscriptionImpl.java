package edu.scut.cs.hm.common.mb;

import java.util.function.Consumer;

class SubscriptionImpl<M> implements Subscription {
    private final SubscriptionsIface<M> subscriptions;
    private final Consumer<M> consumer;

    public SubscriptionImpl(SubscriptionsIface<M> subscriptions, Consumer<M> consumer) {
        this.subscriptions = subscriptions;
        this.consumer = consumer;
    }

    @Override
    public Consumer<?> getConsumer() {
        return this.consumer;
    }

    @Override
    public void close() {
        this.subscriptions.unsubscribe(this.consumer);
    }
}
