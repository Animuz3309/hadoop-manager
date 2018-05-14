package edu.scut.cs.hm.common.utils;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Utility wrapper which present some property, contains default value, and also record first it changes.
 */
public final class Keeper<T> implements Supplier<T>, Consumer<T>, Changeable {
    private T value;
    private boolean changed;

    public Keeper() {
    }

    public Keeper(T value) {
        this.value = value;
    }

    @Override
    public boolean isChanged() {
        return changed;
    }

    @Override
    public void accept(T value) {
        if(!changed) {
            changed = true;
        }
        this.value = value;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Keeper)) {
            return false;
        }

        Keeper<?> keeper = (Keeper<?>) o;

        if (changed != keeper.changed) {
            return false;
        }
        return !(value != null ? !value.equals(keeper.value) : keeper.value != null);

    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (changed ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Keeper{" +
                "value=" + value +
                ", changed=" + changed +
                '}';
    }
}
