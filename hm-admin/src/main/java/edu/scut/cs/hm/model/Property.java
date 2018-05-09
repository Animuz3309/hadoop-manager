package edu.scut.cs.hm.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;

/**
 * a property, getter-setter pair or public object field
 *
 */
public interface Property {
    /**
     * name of property
     * @return
     */
    String getName();

    /**
     * type of property
     * @return
     */
    Class<?> getType();

    /**
     * @return generic Type of property.
     */
    Type getGenericType();

    /**
     * get property value from property owner object
     * @param owner
     * @return
     */
    Object get(Object owner);

    /**
     * is writable
     * @return exists
     */
    boolean isWritable();

    boolean isReadable();

    /**
     * set property value to property owner object
     * @param owner
     * @param value
     */
    void set(Object owner, Object value);

    /**
     * @see Member#getDeclaringClass()
     * @return
     */
    Class<?> getDeclaringClass();
    <T extends Annotation> T getAnnotation(Class<T> annotation);
    boolean isAnnotationPresent(Class<? extends Annotation> annotation);

}
