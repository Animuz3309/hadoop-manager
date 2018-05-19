package edu.scut.cs.hm.common.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import edu.scut.cs.hm.common.utils.Keeper;

import java.io.IOException;
import java.lang.annotation.Annotation;

class KeeperBeanDeserializerModifier extends BeanDeserializerModifier {
    @Override
    public BeanDeserializerBuilder updateBuilder(DeserializationConfig config, BeanDescription beanDesc, BeanDeserializerBuilder builder) {
        for (BeanPropertyDefinition propDef : beanDesc.findProperties()) {
            if (!propDef.hasGetter() || propDef.hasSetter()) {
                continue;
            }
            AnnotatedMember getter = propDef.getAccessor();
            if (!Keeper.class.equals(getter.getRawType())) {
                continue;
            }

            builder.addOrReplaceProperty(new CustomGetterBeanProperty(propDef, getter), true);
        }
        return builder;
    }

    private static class CustomGetterBeanProperty extends SettableBeanProperty {

        private final AnnotatedMember getter;

        public CustomGetterBeanProperty(BeanPropertyDefinition propDef, AnnotatedMember getter) {
            super(propDef, getter.getType(), null, null);
            this.getter = getter;
        }

        protected CustomGetterBeanProperty(CustomGetterBeanProperty orig, PropertyName name) {
            super(orig, name);
            this.getter = orig.getter;
        }

        protected CustomGetterBeanProperty(CustomGetterBeanProperty orig, JsonDeserializer<?> deserializer) {
            super(orig, deserializer, null);
            this.getter = orig.getter;
        }

        @Override
        public CustomGetterBeanProperty withName(PropertyName newName) {
            return new CustomGetterBeanProperty(this, newName);
        }

        @Override
        public SettableBeanProperty withNullProvider(NullValueProvider nullValueProvider) {
            return null;
        }

        @Override
        public CustomGetterBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
            return new CustomGetterBeanProperty(this, deser);
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> acls) {
            return getter.getAnnotation(acls);
        }

        @Override
        public AnnotatedMember getMember() {
            return getter;
        }

        @Override
        @SuppressWarnings("unchecked")
        public final void deserializeAndSet(JsonParser p, DeserializationContext ctxt,
                                            Object instance) throws IOException {

            Keeper target;
            try {
                target = (Keeper) getter.getValue(instance);
            } catch (Exception e) {
                _throwAsIOE(p, e);
                return; // never gets here
            }
            if (target == null) {
                throw JsonMappingException.from(p,
                        "Problem deserialize 'setterless' property '" + getName() + "': get method returned null");
            }

            Object value;
            if (_valueTypeDeserializer != null) {
                value = _valueDeserializer.deserializeWithType(p, ctxt, _valueTypeDeserializer);
            } else {
                value = _valueDeserializer.deserialize(p, ctxt);
            }
            target.accept(value);
        }

        @Override
        public Object deserializeSetAndReturn(JsonParser p,
                                              DeserializationContext ctxt, Object instance) throws IOException {
            deserializeAndSet(p, ctxt, instance);
            return instance;
        }

        @Override
        public final void set(Object instance, Object value) throws IOException {
            throw new UnsupportedOperationException("Should never call 'set' on setterless property");
        }

        @Override
        public Object setAndReturn(Object instance, Object value) throws IOException {
            set(instance, value);
            return null;
        }
    }
}
