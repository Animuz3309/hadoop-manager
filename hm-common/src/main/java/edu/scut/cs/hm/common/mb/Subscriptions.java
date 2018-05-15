package edu.scut.cs.hm.common.mb;

import java.util.function.Consumer;

/**
 * Main part of message bus which allow subscription and can get info of message bus.
 */
public interface Subscriptions<M> extends SubscriptionsIface<M>, MessageBusInfo<M> {

    /**
     * Make empty {@link Subscriptions<T>} which do nothing
     * @param id
     * @param type
     * @param <T>
     * @return
     */
    static <T> Subscriptions<T> empty(String id, Class<T> type) {
        return new Subscriptions<T>() {
            @Override
            public String getId() {
                return id;
            }

            @Override
            public Class<T> getType() {
                return type;
            }

            @Override
            public void subscribe(Consumer<T> listener) {

            }

            @Override
            public void unsubscribe(Consumer<T> listener) {

            }
        };
    }
}
