package edu.scut.cs.hm.common.utils;

import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class Sugar {
    private Sugar() {}

    public static <S> void setIfNotNull(Consumer<S> setter, S val) {
        if (val == null) {
            return;
        }
        setter.accept(val);
    }

    /**
     * Put value into consumer only if value source {@link Changeable#isChanged()}  is changed}.
     * @see Keeper
     * @param consumer
     * @param valueSource
     * @param <S>
     * @param <G>
     */
    public static <S, G extends Supplier<S> & Changeable> void setIfChanged(Consumer<S> consumer, G valueSource) {
        if(valueSource == null || !valueSource.isChanged()) {
            return;
        }
        consumer.accept(valueSource.get());
    }

    /**
     * Make immutable collection, make empty collection for nulls.
     * @param src
     * @param <I>
     * @return
     */
    public static <I> List<I> immutableList(Collection<? extends I> src) {
        return src == null ? ImmutableList.of() : ImmutableList.copyOf(src);
    }

}
