package edu.scut.cs.hm.common.json;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allow to de/serialize enum with lowercase strings.
 */
@JacksonAnnotationsInside
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
@JsonSerialize(converter = EnumToLowercaseStringConverter.class)
@JsonDeserialize(using = EnumLowercaseStringDeserializer.class)
public @interface JtEnumLower {
}
