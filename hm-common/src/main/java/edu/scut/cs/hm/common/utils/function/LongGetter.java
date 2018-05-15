package edu.scut.cs.hm.common.utils.function;

@FunctionalInterface
public interface LongGetter<T> {
    long get(T target);
}
