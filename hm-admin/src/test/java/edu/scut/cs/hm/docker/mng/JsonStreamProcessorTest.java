package edu.scut.cs.hm.docker.mng;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.scut.cs.hm.docker.model.events.DockerEvent;
import org.junit.Test;

import static org.junit.Assert.*;

public class JsonStreamProcessorTest {

    private static final JsonFactory JSON_FACTORY = new JsonFactory();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void test() throws Exception {
        String response = "{\"Type\":\"image\",\"Action\":\"create\"," +
                "\"Actor\":{\"ID\":\"jbopxq4vd008bqgdynqzl4gew\"," +
                "\"Attributes\":{\"name\":\"\"}}," +
                "\"scope\":\"swarm\"," +
                "\"time\":1527060990,\"timeNano\":1527060990284918619}";
        JsonParser jp = JSON_FACTORY.createParser(response);
        JsonToken nextToken = jp.nextToken();
        ObjectNode objectNode = OBJECT_MAPPER.readTree(jp);
        // exclude empty item serialization into class #461
        if (!objectNode.isEmpty(null)) {
            DockerEvent next = OBJECT_MAPPER.treeToValue(objectNode, DockerEvent.class);
            System.out.println(next);
        }
    }
}