package edu.scut.cs.hm.common.mb;

import java.util.function.Consumer;

/**
 * Variant for {@link AutoCloseable}, but without checked exceptions.
 * Represent a subscription on message bus, the backend is a {@link Consumer}
 */
public interface Subscription extends AutoCloseable {

    /**
     * Give consumer which os used for this subscription
     * @return
     */
    Consumer<?> getConsumer();

    /**
     * Extends from {@link AutoCloseable} but without exceptions
     */
    @Override
    void close();
}
