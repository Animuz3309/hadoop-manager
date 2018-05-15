package edu.scut.cs.hm.common.kv;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Extends {@link WriteOptions} to decide whether allow to remove directory usual non empty recursively
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DeleteDirOptions extends WriteOptions {

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Builder extends WriteOptions.Builder<DeleteDirOptions, Builder> {
        private boolean recursive;

        /**
         * Whether allow to remove non empty directory
         * @param recursive
         * @return
         */
        public Builder recursive(boolean recursive) {
            setRecursive(recursive);
            return this;
        }

        @Override
        public DeleteDirOptions build() {
            return new DeleteDirOptions(this);
        }
    }

    private final boolean recursive;

    DeleteDirOptions(Builder b) {
        super(b);
        this.recursive = b.recursive;
    }

    public static Builder builder() {
        return new Builder();
    }
}
