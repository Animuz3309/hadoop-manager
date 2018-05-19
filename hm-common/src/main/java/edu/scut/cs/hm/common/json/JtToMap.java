package edu.scut.cs.hm.common.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface JtToMap {
    /**
     * Property name of key, which can be used in map. <p/>
     * Note that on deserialization property will be set from this.
     * @return name of prop
     */
    String key();
}

