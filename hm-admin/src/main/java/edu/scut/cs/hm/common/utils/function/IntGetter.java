package edu.scut.cs.hm.common.utils.function;

@FunctionalInterface
public interface IntGetter<T> {
    int get(T target);
}
