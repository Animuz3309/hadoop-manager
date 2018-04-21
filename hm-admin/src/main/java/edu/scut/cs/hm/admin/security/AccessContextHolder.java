package edu.scut.cs.hm.admin.security;

/**
 * Hold the {@link AccessContext}
 */
public final class AccessContextHolder implements AutoCloseable {

    private final AccessContext context;
    private final Runnable onClose;

    AccessContextHolder(AccessContext context, Runnable onClose) {
        this.context = context;
        this.onClose = onClose;
    }

    public AccessContext getContext() {
        return context;
    }

    @Override
    public void close() {
        this.onClose.run();
    }
}
