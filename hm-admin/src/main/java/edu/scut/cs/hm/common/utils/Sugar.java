package edu.scut.cs.hm.common.utils;

import java.util.function.Consumer;

public final class Sugar {
    private Sugar() {}

    public static <S> void setIfNotNull(Consumer<S> setter, S val) {
        if (val == null) {
            return;
        }
        setter.accept(val);
    }
}
