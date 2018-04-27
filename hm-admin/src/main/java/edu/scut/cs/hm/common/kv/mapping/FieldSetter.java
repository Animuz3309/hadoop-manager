package edu.scut.cs.hm.common.kv.mapping;

/**
 * Interface for modifying value of final fields
 * @param <T> final fields object type
 */
public interface FieldSetter<T> {
    void set(T fieldValue, T newValue);
}
