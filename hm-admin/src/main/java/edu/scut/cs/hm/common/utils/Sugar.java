package edu.scut.cs.hm.common.utils;

import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public final class Sugar {
    private Sugar() {}

    public static <S> void setIfNotNull(Consumer<S> setter, S val) {
        if (val == null) {
            return;
        }
        setter.accept(val);
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
