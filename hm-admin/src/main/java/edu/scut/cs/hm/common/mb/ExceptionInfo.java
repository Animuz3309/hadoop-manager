package edu.scut.cs.hm.common.mb;

import java.util.function.Consumer;

/**
 * Object which hold info about exception which is caused from message consumers.
 * <p/>
 * Note that consumer which is handle this class must be safe.
 */
public class ExceptionInfo {
    private final MessageBusInfo bus;
    private final Throwable throwable;
    private final Consumer<?> consumer;
    private final Object message;

    ExceptionInfo(MessageBusInfo bus, Throwable throwable, Consumer<?> consumer, Object message) {
        this.bus = bus;
        this.throwable = throwable;
        this.consumer = consumer;
        this.message = message;
    }

    public MessageBusInfo getBus() {
        return bus;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public Consumer<?> getConsumer() {
        return consumer;
    }

    public Object getMessage() {
        return message;
    }
}
