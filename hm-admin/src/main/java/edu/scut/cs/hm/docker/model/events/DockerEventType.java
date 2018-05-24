package edu.scut.cs.hm.docker.model.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Type of {@link DockerEvent}
 */
public enum DockerEventType {

    /**
     * @since 1.24
     */
    CONTAINER("container"),
    /**
     * @since 1.24
     */
    DAEMON("daemon"),
    /**
     * @since 1.24
     */
    IMAGE("image"),
    NETWORK("network"),
    PLUGIN("plugin"),
    VOLUME("volume"),
    NODE("node"),
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
