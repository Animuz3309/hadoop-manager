package edu.scut.cs.hm.docker.model.network;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.NullNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@JsonSerialize(using = Links.Serializer.class)
@JsonDeserialize(using = Links.Deserializer.class)
public class Links {

    private final List<Link> links;

    public Links(final Link... links) {
        this.links = Arrays.asList(links);
    }

    public Links(final List<Link> links) {
        this.links = links;
    }

    public List<Link> getLinks() {
        return links;
    }

    public static class Serializer extends JsonSerializer<Links> {

        @Override
        public void serialize(final Links links, final JsonGenerator jsonGen, final SerializerProvider serProvider)
                throws IOException {
            jsonGen.writeStartArray();
            for (final Link link : links.getLinks()) {
                jsonGen.writeString(link.toString());
            }
            jsonGen.writeEndArray();
        }

    }

    public static class Deserializer extends JsonDeserializer<Links> {

        @Override
        public Links deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext)
                throws IOException {
            final List<Link> binds = new ArrayList<Link>();
            final ObjectCodec oc = jsonParser.getCodec();
            final JsonNode node = oc.readTree(jsonParser);
            for (final Iterator<JsonNode> it = node.elements(); it.hasNext(); ) {

                final JsonNode element = it.next();
                if (!element.equals(NullNode.getInstance())) {
                    binds.add(Link.parse(element.asText()));
                }
            }
            return new Links(binds.toArray(new Link[0]));
        }
    }
}
