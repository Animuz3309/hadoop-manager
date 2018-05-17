package edu.scut.cs.hm.common.utils;

import java.util.function.Predicate;

/**
 */
public final class Predicates {

    public static final Predicate<Object> TRUE = o -> true;
    public static final Predicate<Object> FALSE = o -> false;

    private Predicates() {
    }

    /**
     *
     * @param <T>
     * @return {@link #TRUE}
     */
    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> truePredicate() {
        return (Predicate<T>) TRUE;
    }

    /**
     *
     * @param <T>
     * @return {@link #FALSE}
     */
    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> falsePredicate() {
        return (Predicate<T>) FALSE;
    }
}
