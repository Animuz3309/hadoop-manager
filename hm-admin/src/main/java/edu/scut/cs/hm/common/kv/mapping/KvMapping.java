package edu.scut.cs.hm.common.kv.mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation of object fileds, which define mapping between fields and key value storage records. <p/>
 * Note that complex values (like list or map) must be immutable.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface KvMapping {
    /**
     * Canonical type representation. It use fore json deserialization.
     * @see com.fasterxml.jackson.databind.type.TypeFactory#constructFromCanonical(String)
     * @see com.fasterxml.jackson.databind.JavaType#toCanonical()
     * @return
     */
    String type() default "";

    /**
     * Array of property interceptors which called in same order at save, and reverse order at read
     * @return
     */
    Class<? extends PropertyInterceptor>[] interceptors() default {};
}
