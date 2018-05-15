package edu.scut.cs.hm.common.mb;

import java.util.function.Consumer;

/**
 * Message bus which can do subscribe
 * @param <M>
 */
public interface MessageBus<M> extends Consumer<M>, Subscriptions<M>, AutoCloseable {

    /**
     * Provide part of api which allow only subscription.
     * Provide a Subscriptions to subscribe listener({@link Consumer})
     * @see edu.scut.cs.hm.common.mb.Subscriptions
     * @return
     */
    Subscriptions<M> asSubscriptions();

    /**
     * Mean that does not have any subscriber.
     * @return
     */
    boolean isEmpty();
}
