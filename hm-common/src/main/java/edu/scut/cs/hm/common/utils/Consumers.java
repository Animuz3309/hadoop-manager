package edu.scut.cs.hm.common.utils;

import java.util.function.Consumer;

/**
 * Some utilities for {@link java.util.function.Consumer }
 */
public final class Consumers {
    private Consumers() {}

    /**
     * Holder for consumed value. It threadsafe.
     * @param <T>
     * @return
     */
    public static <T> HolderConsumer<T> holder() {
        return new HolderConsumer<>();
    }

    /**
     * Holder for consumed value. It threadsafe.
     * @param <T>
     */
    public static class HolderConsumer<T> implements Consumer<T> {
        private volatile T value;


        @Override
        public void accept(T t) {
            this.value = t;
        }

        /**
         * Last accepted value.
         * @return
         */
        public T getValue() {
            return value;
        }
    }

    public static final Consumer<Object> NO_OP = o -> {
        //nothing
    };

    /**
     * Consumer which do nothing 'No OPerations'
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Consumer<T> nop() {
        return (Consumer<T>) NO_OP;
    }
}
