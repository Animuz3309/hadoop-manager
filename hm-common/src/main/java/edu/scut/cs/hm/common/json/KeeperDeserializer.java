package edu.scut.cs.hm.common.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.type.TypeBindings;
import edu.scut.cs.hm.common.utils.Keeper;
import org.springframework.util.Assert;

import java.io.IOException;

class KeeperDeserializer extends JsonDeserializer<Object> implements ContextualDeserializer {

    private final BeanProperty property;

    public KeeperDeserializer() {
        this.property = null;
    }

    private KeeperDeserializer(BeanProperty property) {
        this.property = property;
    }

    /**
     * When we use plain deserializer we must return plain value. Setting into keeper will done in
     * external code {@link KeeperBeanDeserializerModifier.CustomGetterBeanProperty }
     * @param p
     * @param ctxt
     * @return
     * @throws IOException
     */
    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return getInternalValue(p, ctxt);
    }

    private Object getInternalValue(JsonParser p, DeserializationContext ctxt) throws IOException {
        JavaType type = ctxt.getContextualType();
        if(type == null) {
            if(property == null) {
                throw new IllegalStateException("can not deserialize value with null property");
            } else {
                type = property.getType();
            }
        }
        type = resolve(type);
        return p.readValueAs(type.getRawClass());
    }

    private JavaType resolve(final JavaType type) {
        Assert.notNull(type, "type can't be null");
        JavaType tmp = type;
        while(Keeper.class.equals(tmp.getRawClass())) {
            TypeBindings bindings = tmp.getBindings();
            Assert.isTrue(bindings.size() == 1, "Bindings must have one parameter type: " + type);
            tmp = bindings.getBoundType(0);
        }
        return tmp;
    }

    /**
     * When we used intovalue, then we must return keeper
     * @param p
     * @param ctxt
     * @param intoValue
     * @return
     * @throws IOException
     * @throws JsonProcessingException
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object deserialize(JsonParser p, DeserializationContext ctxt, Object intoValue) throws IOException {
        Object value = getInternalValue(p, ctxt);
        ((Keeper)intoValue).accept(value);
        return intoValue;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        return new KeeperDeserializer(property);
    }
}