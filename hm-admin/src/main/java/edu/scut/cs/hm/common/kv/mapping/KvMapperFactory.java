package edu.scut.cs.hm.common.kv.mapping;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.ImmutableMap;
import edu.scut.cs.hm.common.kv.KeyValueStorage;
import edu.scut.cs.hm.common.utils.FindHandlerUtil;
import edu.scut.cs.hm.common.validate.JsrValidityImpl;
import edu.scut.cs.hm.common.validate.Validity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.util.Assert;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Factory to get Key-value mapper like {@link KvClassMapper}
 */
@Slf4j
public class KvMapperFactory {
    private final ObjectMapper objectMapper;
    private final KeyValueStorage storage;
    private final Map<Class<?>, FieldSetter> setters;
    private final Map<Class<?>, PropertyInterceptor> interceptors;
    private final Validator validator;

    @SuppressWarnings("unchecked")
    public KvMapperFactory(ObjectMapper objectMapper,
                           KeyValueStorage storage,
                           TextEncryptor encryptor,
                           Validator validator) {
        this.objectMapper = objectMapper;
        this.storage = storage;
        this.validator = validator;

        ImmutableMap.Builder<Class<?>, FieldSetter> builder = ImmutableMap.builder();
        builder.put(Map.class, (field, value) -> {
            Map fieldMap = (Map) field;
            fieldMap.clear();
            if (value != null) {
                fieldMap.putAll((Map) value);
            }
        });
        builder.put(Collection.class, (field, value) -> {
            Collection fieldColl = (Collection) field;
            fieldColl.clear();
            fieldColl.addAll((Collection) value);
        });
        setters = builder.build();
        interceptors = ImmutableMap.<Class<?>, PropertyInterceptor>builder()
                .put(PropertyCipher.class, new PropertyCipher(encryptor))
                .build();
    }

    /**
     * load property from the class annotation by {@link KvMapping}
     * @param clazz
     * @param func
     * @param <T>
     * @return
     */
    <T> Map<String, T> loadProps(Class<?> clazz, Function<KvProperty, T> func) {
        ImmutableMap.Builder<String, T> b = ImmutableMap.builder();
        TypeFactory tf = TypeFactory.defaultInstance();
        while(clazz != null && !Object.class.equals(clazz)) {
            for(Field field: clazz.getDeclaredFields()) {
                KvMapping mapping = field.getAnnotation(KvMapping.class);
                if (mapping == null) {
                    continue;
                }
                JavaType javaType;
                String typeStr = mapping.type();
                if (!typeStr.isEmpty()) {
                    javaType = tf.constructFromCanonical(typeStr);
                } else {
                    javaType = tf.constructType(field.getGenericType());
                }
                KvProperty property = new KvProperty(this, field.getName(), field, javaType);
                b.put(property.getKey(), func.apply(property));
            }
        }
        return b.build();
    }

    @SuppressWarnings("unchecked")
    <T> FieldSetter<T> getSetter(Class<T> type) {
        return FindHandlerUtil.findByClass(type, setters);
    }

    <T> AbstractMapping<T> getMapping(Class<T> type, KvObjectFactory<T> factory) {
        AbstractMapping<T> mapping = NodeMapping.makeIfHasProps(this, type, factory);
        if(mapping == null) {
            mapping = new LeafMapping<>(this, type);
        }
        return mapping;
    }

    /**
     * Get json ObjectMapper
     * @return
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * Get k-v storage
     */
    public KeyValueStorage getStorage() {
        return storage;
    }

    public <T>  KvClassMapper<T> createClassMapper(String prefix, Class<T> type) {
        return KvClassMapper.builder(this, type).prefix(prefix).build();
    }

    public <T>  KvClassMapper.Builder<T> buildClassMapper(Class<T> type) {
        return KvClassMapper.builder(this, type);
    }

    public PropertyInterceptor[] getInterceptors(Class<? extends PropertyInterceptor>[] classes) {
        PropertyInterceptor[] instances = new PropertyInterceptor[classes.length];
        for(int i = 0; i < classes.length; ++i) {
            Class<? extends PropertyInterceptor> type = classes[i];
            PropertyInterceptor instance = this.interceptors.get(type);
            Assert.notNull(instance, "can not find interceptor of type: " + type);
            instances[i] = instance;
        }
        return instances;
    }

    public <T> Validity validate(String path, T object) {
        Set<ConstraintViolation<T>> res = validator.validate(object);
        Validity validity = new JsrValidityImpl(path, res);
        if(!validity.isValid()) {
            log.warn("Invalid {}", validity.getMessage());
        }
        return validity;
    }
}
