package edu.scut.cs.hm.docker.model.network;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.NullNode;
import edu.scut.cs.hm.common.utils.Sugar;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.IOException;
import java.util.*;

@JsonSerialize(using = ExposedPorts.Serializer.class)
@JsonDeserialize(using = ExposedPorts.Deserializer.class)
@EqualsAndHashCode
@ToString
@Getter
public class ExposedPorts {

    private final List<ExposedPort> exposedPorts;

    public ExposedPorts(ExposedPort... exposedPorts) {
        this.exposedPorts = Sugar.immutableList(Arrays.asList(exposedPorts));
    }

    public ExposedPorts(Collection<ExposedPort> exposedPorts) {
        this.exposedPorts = Sugar.immutableList(exposedPorts);
    }

    public static class Serializer extends JsonSerializer<ExposedPorts> {

        @Override
        public void serialize(ExposedPorts exposedPorts, JsonGenerator jsonGen, SerializerProvider serProvider)
                throws IOException {

            jsonGen.writeStartObject();
            for (ExposedPort exposedPort : exposedPorts.getExposedPorts()) {
                jsonGen.writeFieldName(exposedPort.toString());
                jsonGen.writeStartObject();
                jsonGen.writeEndObject();
            }
            jsonGen.writeEndObject();
        }

    }

    public static class Deserializer extends JsonDeserializer<ExposedPorts> {
        @Override
        public ExposedPorts deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {

            List<ExposedPort> exposedPorts = new ArrayList<>();
            ObjectCodec oc = jsonParser.getCodec();
            JsonNode node = oc.readTree(jsonParser);
            for (Iterator<Map.Entry<String, JsonNode>> it = node.fields(); it.hasNext(); ) {

                Map.Entry<String, JsonNode> field = it.next();
                if (!field.getValue().equals(NullNode.getInstance())) {
                    exposedPorts.add(ExposedPort.parse(field.getKey()));
                }
            }
            return new ExposedPorts(exposedPorts.toArray(new ExposedPort[0]));
        }
    }
}
