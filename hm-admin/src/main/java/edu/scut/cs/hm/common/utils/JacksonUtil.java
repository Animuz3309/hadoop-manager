package edu.scut.cs.hm.common.utils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import java.util.TimeZone;

/**
 * Jackson utils to get Jackson {@link ObjectMapper}
 */
public final class JacksonUtil {

    public static final String JSON_DATETIME_FORMAT_PATTERN = "yyyy/MM/dd HH:mm:ss";
    public static final TimeZone DEFAULT_TIMEZONE = TimeZone.getDefault();
    private JacksonUtil() {}

    /**
     * Build Jackson ObjectMapper with default for our customize settings <p/>
     * This settings can be override by @JsonInclude, @JsonFormat and so on, here just the global settings
     * @return
     */
    public static ObjectMapper objectMapperBuilder(){
        ObjectMapper objectMapper = new ObjectMapper()
                // 如果序列化时报错，则返回空bean
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                // 反序列化时，遇到json中的属性在bean中没有对应的setter，不报错
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                // 序列化时，不要将java时间属性转变成timestamp
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        registerModules(objectMapper);

        // 全局设置，如果map是null或者不存在非null的值，输出{}，如果即存在null/非null值，null值不输出
        // e.g. Map a = {a=1, b=null} -> {"a":"1"}
        JsonInclude.Value v = JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, JsonInclude.Include.NON_NULL);
        objectMapper.configOverride(java.util.Map.class).setInclude(v);

        // 全局设置解析的时区和format
        objectMapper.configOverride(java.util.Date.class)
                .setFormat(JsonFormat.Value.forPattern(JSON_DATETIME_FORMAT_PATTERN));
        objectMapper.configOverride(java.time.LocalDateTime.class)
                .setFormat(JsonFormat.Value.forPattern(JSON_DATETIME_FORMAT_PATTERN));
        objectMapper.setTimeZone(DEFAULT_TIMEZONE);

        objectMapper.setSerializationInclusion(JsonInclude.Include.USE_DEFAULTS);
        return objectMapper;
    }

    public static void registerModules(ObjectMapper objectMapper) {
        objectMapper.registerModules(
                new ParameterNamesModule(), // support for detecting constructor and factory method ("creator") parameters
                new Jdk8Module(),           // support for other new Java 8 datatypes outside of date/time
                new JavaTimeModule());      // support for Java 8 date/time types
    }
}
