package edu.scut.cs.hm.docker.res;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.function.Consumer;

/**
 * Process event entry, for async call watch
 */
@Data
@Slf4j
public class ProcessEvent {
    private final Long time;
    private final String message;

    @JsonCreator
    public ProcessEvent(@JsonProperty("time") Long time, @JsonProperty("message") String message) {
        this.time = time;
        this.message = message;
    }

    /**
     * Accept null watcher and use {@link MessageFormat} for interpolating 'event' with args.
     * @param watcher
     * @param msg a string or template for {@link MessageFormat}
     * @param args
     */
    public static void watch(Consumer<ProcessEvent> watcher, String msg, Object ... args) {
        String formatted;
        if(args.length > 0) {
            formatted = MessageFormat.format(msg, args);
        } else {
            formatted = msg;
        }
        watchRaw(watcher, formatted, true);
    }

    /**
     * Pass message without any processing to watcher if watcher is not null. If watcher is null do nothing.
     * @param watcher
     * @param msg
     * @param addTime to event
     */
    public static void watchRaw(Consumer<ProcessEvent> watcher, String msg, boolean addTime) {
        ProcessEvent event = new ProcessEvent(addTime ? System.currentTimeMillis() : null, msg);
        // below line in some cases send to many info into log, also it put log from each container to our log
        //log.info("added event {}", event);
        if(watcher != null) {
            watcher.accept(event);
        }
    }

    @Override
    public String toString() {
        return message;
    }
}