package edu.scut.cs.hm.common.kv;

import lombok.Data;
import org.springframework.util.Assert;

/**
 * Options of write operation <p/>
 * Some Key-Value volume engine may not implement all set of options.
 */
@Data
public class WriteOptions {

    @Data
    public static class Builder<T extends WriteOptions, B extends Builder<T, B>> {
        private int prevIndex = -1;
        private boolean failIfExists = false;
        private boolean failIfAbsent = false;
        private long ttl = -1;

        @SuppressWarnings("unchecked")
        protected B thiz() {
            return (B) this;
        }

        public B prevIndex(int prevIndex) {
            setPrevIndex(prevIndex);
            return thiz();
        }

        public B failIfExists(boolean failIfExists) {
            setFailIfExists(failIfExists);
            return thiz();
        }

        public B failIfAbsent(boolean failIfAbsent) {
            setFailIfAbsent(failIfAbsent);
            return thiz();
        }

        public B ttl(long ttl) {
            setTtl(ttl);
            return thiz();
        }

        public WriteOptions build() {
            return new WriteOptions(this);
        }
    }

    private final int prevIndex;
    private final boolean failIfExists;
    private final boolean failIfAbsent;
    private final long ttl;

    WriteOptions(Builder b) {
        this.prevIndex = b.prevIndex;
        this.failIfAbsent = b.failIfAbsent;
        this.failIfExists = b.failIfExists;
        Assert.isTrue(!this.failIfAbsent || !this.failIfExists,
                "Simultaneous set of both flags 'failIfAbsent'=" + this.failIfAbsent +
                                                   " and 'failIfExists'=" + this.failIfExists +
                        " to true is incorrect.");
        this.ttl = b.ttl;
    }

    public static Builder builder() {
        return new Builder();
    }
}
