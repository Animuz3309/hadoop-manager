package edu.scut.cs.hm.common.mb;

import java.util.function.Consumer;

/**
 * Iface for listeners which listen subscribe/unsubscribe event
 * @param <M>
 */
public interface SubscribeListener<M> {

    /**
     *
     * @param bus
     * @param listener note that value may be wrapped listener
     */
    void event(MessageBus<M> bus, Consumer<M> listener);
}
