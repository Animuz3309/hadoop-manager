package edu.scut.cs.hm.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

/**
 * Base log event
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LogEvent extends Event implements WithSeverity, WithAction {

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static abstract class Builder<B extends Builder<B, T>, T extends LogEvent> extends Event.Builder<B, T> {
        private String user;
        private Severity severity;
        private String action;
        private String name;
        private String message;

        public B user(String user) {
            setUser(user);
            return thiz();
        }

        public B severity(Severity severity) {
            setSeverity(severity);
            return thiz();
        }

        public B action(String action) {
            setAction(action);
            return thiz();
        }

        public B name(String name) {
            setName(name);
            return thiz();
        }

        public B message(String message) {
            setMessage(message);
            return thiz();
        }
    }

    private final String kind = StringUtils.uncapitalize(getClass().getSimpleName());

    private final String user;
    private final Severity severity;
    private final String action;
    private final String name;
    private final String message;

    public LogEvent(Builder<?, ?> b) {
        super(b);
        this.user = b.user;
        this.severity = b.severity;
        this.action = b.action;
        this.name = b.name;
        this.message = b.message;
    }
}
