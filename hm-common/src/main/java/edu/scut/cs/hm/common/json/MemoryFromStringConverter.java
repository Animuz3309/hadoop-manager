package edu.scut.cs.hm.common.json;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import edu.scut.cs.hm.common.utils.DataSize;

/**
 */
class MemoryFromStringConverter implements Converter {
    @Override
    public Object convert(Object value) {
        return DataSize.fromString((String) value);
    }

    @Override
    public JavaType getInputType(TypeFactory typeFactory) {
        return typeFactory.constructType(String.class);
    }

    @Override
    public JavaType getOutputType(TypeFactory typeFactory) {
        return typeFactory.constructType(Long.class);
    }
}
