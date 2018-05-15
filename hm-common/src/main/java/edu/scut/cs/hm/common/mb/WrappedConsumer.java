package edu.scut.cs.hm.common.mb;

import java.util.function.Consumer;

/**
 * Wrapped {@link Consumer}
 * @param <T>
 */
public interface WrappedConsumer<T> extends Consumer<T>, AutoCloseable {

    /**
     * Return internal object, which is wrapped by this
     * @return
     */
    Consumer<T> unwrap();

    /**
     * Used when bus contains wrapped listeners(Consumer) <p/>
     * note. this method must be work correct if argument is not a wrapper.
     * @param src
     * @param <M>
     * @return
     */
    static <M> Consumer<M> unwrap(Consumer<M> src) {
        Consumer<M> tmp = src;
        while (tmp instanceof WrappedConsumer) {
            tmp = ((WrappedConsumer<M>) tmp).unwrap();
            if (tmp == null) {
                throw new IllegalArgumentException("Null consumer in " + src);
            }
        }
        return tmp;
    }
}
