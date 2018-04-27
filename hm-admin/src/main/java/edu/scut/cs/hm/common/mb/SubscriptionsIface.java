package edu.scut.cs.hm.common.mb;

import java.util.function.Consumer;

/**
 * Common interface for any object which is support for subscription ({@link Subscription})
 */
public interface SubscriptionsIface<M> {

    /**
     * Method which subscribe specified listener on current bus.
     * If listener already subscribed then do nothing
     * @param listener
     */
    void subscribe(Consumer<M> listener);

    /**
     * Act like {@link #subscribe(Consumer)}, but return closeable {@link Subscription} which can do unsubscribe in its
     * {@link Subscription#close()} method
     * @param listener
     * @return
     */
    default Subscription openSubscription(Consumer<M> listener) {
        subscribe(listener);
        return new SubscriptionImpl<M>(this, listener);
    }

    /**
     * Method which unsubscribe specified listener from current bus.
     * If listener not subscribe then doing nothing
     * @param listener
     */
    void unsubscribe(Consumer<M> listener);
}
