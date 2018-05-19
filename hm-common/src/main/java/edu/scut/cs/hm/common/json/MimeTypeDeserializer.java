package edu.scut.cs.hm.common.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.io.IOException;

/**
 * Allow to create MimeType object form human readable view of MimeType
 */
class MimeTypeDeserializer extends JsonDeserializer<MimeType> {
    @Override
    public MimeType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String mimeString = p.readValueAs(String.class);
        return MimeTypeUtils.parseMimeType(mimeString);
    }
}