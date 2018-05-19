package edu.scut.cs.hm.common.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.util.MimeType;

import java.io.IOException;

/**
 * Allow to create human readable view of MimeType
 */
class MimeTypeSerializer extends JsonSerializer<MimeType> {

    @Override
    public void serialize(MimeType value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(value.toString());
    }
}
