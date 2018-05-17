package edu.scut.cs.hm.common.fc;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.util.Assert;

import java.io.IOException;

public class FbJacksonAdapter<T> implements FbAdapter<T> {

    private final ObjectMapper objectMapper;
    private final Class<T> type;

    @Data
    private static class Wrapper {
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
        private Object object;
    }

    public FbJacksonAdapter(ObjectMapper objectMapper, Class<T> type) {
        this.objectMapper = objectMapper;
        this.type = type;
        Assert.notNull(this.objectMapper, "objectMapper is null");
        Assert.notNull(this.type, "type is null");
    }

    @Override
    public byte[] serialize(T obj) throws IOException {
        type.cast(obj);
        Wrapper wrapper = new Wrapper();
        wrapper.setObject(obj);
        return objectMapper.writeValueAsBytes(wrapper);
    }

    @Override
    public T deserialize(byte[] data, int offset, int len) throws IOException {
        Wrapper wrapper = objectMapper.readValue(data, offset, len, Wrapper.class);
        return type.cast(wrapper.getObject());
    }
}
