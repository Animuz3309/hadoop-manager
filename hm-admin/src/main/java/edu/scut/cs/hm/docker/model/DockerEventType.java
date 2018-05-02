package edu.scut.cs.hm.docker.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import edu.scut.cs.hm.model.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * Type of {@link DockerEvent}
 */
public enum DockerEventType {

    CONTAINER("container"),
    DAEMON("daemon"),
    IMAGE("image"),
    NETWORK("network"),
    PLUGIN("plugin"),
    VOLUME("volume"),
    ;
    private static final Map<String, DockerEventType> EVENT_TYPES = new HashMap<>();

    static {
        for (DockerEventType t : values()) {
            EVENT_TYPES.put(t.name().toLowerCase(), t);
        }
    }

    private String value;

    DockerEventType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static DockerEventType forValue(String s) {
        return EVENT_TYPES.get(s);
    }
}
