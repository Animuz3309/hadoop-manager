package edu.scut.cs.hm.docker.model.container;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LxcConfig {
    @JsonProperty("Key")
    public String key;

    @JsonProperty("Value")
    public String value;

    public LxcConfig(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public LxcConfig() {
    }

    public String getKey() {
        return key;
    }

    public LxcConfig setKey(String key) {
        this.key = key;
        return this;
    }

    public String getValue() {
        return value;
    }

    public LxcConfig setValue(String value) {
        this.value = value;
        return this;
    }

}

