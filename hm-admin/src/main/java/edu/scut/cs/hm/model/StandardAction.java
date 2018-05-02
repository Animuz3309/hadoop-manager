package edu.scut.cs.hm.model;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Standard actions over objects.
 */
public enum StandardAction {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    START("start"),
    STOP("stop"),
    DIE("die"),
    ONLINE("online"),
    OFFLINE("offline"),
    ;

    private String value;

    StandardAction(java.lang.String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    private static final Map<String, Severity> severityMap = ImmutableMap.<String, Severity>builder()
            .put(DIE.value, Severity.ERROR)
            .put(OFFLINE.value, Severity.WARNING)
            .build();

    public static Severity toSeverity(String action) {
        Severity severity = severityMap.get(action);
        if(severity == null) {
            severity = Severity.INFO;
        }
        return severity;
    }
}
