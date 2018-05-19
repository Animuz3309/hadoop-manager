package edu.scut.cs.hm.common.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;

class EnumLowercaseStringDeserializer extends JsonDeserializer<Enum<?>> implements ContextualDeserializer {

    private final BeanProperty property;

    public EnumLowercaseStringDeserializer() {
        this.property = null;
    }

    private EnumLowercaseStringDeserializer(BeanProperty property) {
        this.property = property;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Enum<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String name = p.getValueAsString();
        if(StringUtils.isEmpty(name)) {
            return null;
        }
        JavaType type;
        if(property != null) {
            type = property.getType();
        } else {
            type = ctxt.getContextualType();
        }
        Assert.notNull(type, "Type of current property is null.");
        Class clazz = type.getRawClass();
        Assert.isTrue(clazz.isEnum(), "The " + clazz + " is not an enum type.");
        return Enum.valueOf(clazz, name.toUpperCase());
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        return new EnumLowercaseStringDeserializer(property);
    }
}
