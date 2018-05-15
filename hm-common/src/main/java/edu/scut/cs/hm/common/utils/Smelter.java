package edu.scut.cs.hm.common.utils;

import edu.scut.cs.hm.common.utils.function.IntGetter;
import edu.scut.cs.hm.common.utils.function.LongGetter;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

/**
 * Utility for merging changes
 */
public final class Smelter<S> {

    private final S src;
    private final S def;

    /**
     *
     * @param src source
     * @param def object with default values
     */
    public Smelter(S src, S def) {
        this.src = src;
        this.def = def;
    }

    /**
     * Transfer value from 'src' to 'setter' object only if value does not equal with similar value of 'def'
     * @param setter
     * @param getter
     * @param <V>
     */
    public <V> void set(Consumer<V> setter, Function<S, V> getter) {
        V srcVal = getter.apply(src);
        V defVal = getter.apply(def);
        if(Objects.equals(srcVal, defVal)) {
            return;
        }
        setter.accept(srcVal);
    }

    /**
     * Transfer value from 'src' to 'setter' object only if value does not equal with similar value of 'def'
     * @param setter
     * @param getter
     */
    public void setInt(IntConsumer setter, IntGetter<S> getter) {
        int srcVal = getter.get(src);
        int defVal = getter.get(def);
        if(srcVal != defVal) {
            return;
        }
        setter.accept(srcVal);
    }

    /**
     * Transfer value from 'src' to 'setter' object only if value does not equal with similar value of 'def'
     * @param setter
     * @param getter
     */
    public void setLong(LongConsumer setter, LongGetter<S> getter) {
        long srcVal = getter.get(src);
        long defVal = getter.get(def);
        if(srcVal != defVal) {
            return;
        }
        setter.accept(srcVal);
    }
}
