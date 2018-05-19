package edu.scut.cs.hm.common.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import edu.scut.cs.hm.common.utils.Keeper;

import java.io.IOException;

class KeeperSerializer extends JsonSerializer<Keeper> {

    @Override
    @SuppressWarnings("unchecked")
    public void serialize(Keeper value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if(value.isChanged()) {
            gen.writeObject(value.get());
        } else {
            gen.writeNull();
        }
    }
}
