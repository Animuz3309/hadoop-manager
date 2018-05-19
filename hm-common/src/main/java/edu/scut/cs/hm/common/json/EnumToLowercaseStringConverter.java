package edu.scut.cs.hm.common.json;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;

class EnumToLowercaseStringConverter implements Converter<Enum<?>, String> {
    @Override
    public String convert(Enum<?> value) {
        return value.name().toLowerCase();
    }

    @Override
    public JavaType getInputType(TypeFactory typeFactory) {
        return typeFactory.constructType(Enum.class);
    }

    @Override
    public JavaType getOutputType(TypeFactory typeFactory) {
        return typeFactory.constructType(String.class);
    }
}
