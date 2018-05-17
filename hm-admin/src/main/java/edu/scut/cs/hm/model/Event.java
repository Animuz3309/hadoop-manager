package edu.scut.cs.hm.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Base64;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Base class for event
 */
@Data
public class Event implements EventWithTime {

    @Data
    public static abstract class Builder<B extends Builder<B, T>, T extends Event> {

        private String id = newLiteUid();
        private Date date = new Date();

        @SuppressWarnings("unchecked")
        protected B thiz() {
            return (B)this;
        }

        public B id(String id) {
            setId(id);
            return thiz();
        }

        public B date(Date date) {
            setDate(date);
            return thiz();
        }

        public abstract T build();

    }

    private static String newLiteUid() {
        ByteBuffer bb = ByteBuffer.allocate(16).order(ByteOrder.BIG_ENDIAN);
        bb.putLong(INSTANCE_ID);
        bb.putLong(COUNTER.getAndIncrement());
        return Base64.getUrlEncoder().encodeToString(bb.array());
    }

    private static final AtomicLong COUNTER = new AtomicLong();
    private static final long INSTANCE_ID = new Random().nextLong();
    private final String id;
    private final Date date;

    @JsonCreator
    public Event(Builder<?, ?> b) {
        this.id = b.id;
        this.date = b.date;
    }

    @Override
    public long getTimeInMilliseconds() {
        return date == null? Long.MIN_VALUE : date.getTime();
    }
}
