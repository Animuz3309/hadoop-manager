package edu.scut.cs.hm.docker.model;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.scut.cs.hm.common.utils.JacksonUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.junit.Test;

import java.util.List;

public class ModelJsonTest {

    private ObjectMapper objectMapper = JacksonUtils.objectMapperBuilder();

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    @Builder
    @AllArgsConstructor(onConstructor = @__(@JsonCreator))
    private static class Model {
        @JsonProperty("Flag")
        private final Boolean flag;
        @JsonProperty("ListStr")
        private final List<String> listStr;
        @JsonProperty("Name")
        private final String name;
    }

    @Test
    public void testModel() throws Exception {
        String json = "{" +
                "\"Flag\": \"false\"," +
                "\"ListStr\": [\"s1\", \"s2\"]," +
                "\"Name\": \"Test\"" +
                "}";
        objectMapper.readValue(json, Model.class);
    }

    public void testContainerConfig() {

    }
}