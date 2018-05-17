package edu.scut.cs.hm.common.mb;

import edu.scut.cs.hm.common.utils.Predicates;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Extension iface for sonsumer.
 * <p/>
 *  All methods of this iface must be 'default'.
 */
public interface SmartConsumer<T> extends Consumer<T> {

    /**
     * Cast consumer to smart consumer, or return wrapper, which simulate default values.
     * @param consumer
     * @param <T>
     * @return casted instance or default wrapper, never return null
     */
    @SuppressWarnings("unchecked")
    static <T> SmartConsumer<T> of(Consumer<T> consumer) {
        // first we try potentially wrapped consumer
        if(consumer instanceof SmartConsumer) {
            return (SmartConsumer<T>) consumer;
        }
        if(consumer instanceof WrappedConsumer) {
            Consumer<T> orig = WrappedConsumer.unwrap(consumer);
            // then unwrap ant try again
            if(orig != consumer && orig instanceof SmartConsumer) {
                return (SmartConsumer<T>) orig;
            }
        }
        // we assume that all methods of this iface must be 'default'
        return consumer::accept;
    }

    /**
     * Specify count of last history entries which is passed into consumer at subscription.<p/>
     * Some implementation may support only full count, but any implementation must consider
     * '0' - as value which is disable history.
     * @return {@link Integer#MAX_VALUE} for read full history (default), 0 for disable history
     */
    default int getHistoryCount() {
        return Integer.MAX_VALUE;
    }

    /**
     * Predicate which must return true for passed entries, and false for skipped. <p/>
     * @see Predicates#truePredicate()
     * @return never null, default value {@link Predicates#TRUE}
     */
    default Predicate<T> historyFilter() {
        return Predicates.truePredicate();
    }
}