package edu.scut.cs.hm.docker;

import lombok.Data;

@Data
final class OfflineCause {
    /**
     * Used for cases when service never been accessed before. Without this we
     * cannot detect 'cluster online' event at first cluster connection after startup.
     */
    static final OfflineCause INITIAL = new OfflineCause(0, null, Long.MIN_VALUE);

    private final long time;
    private final long timeout;
    private final Throwable throwable;

    OfflineCause(long timeout, Throwable throwable) {
        this(timeout, throwable, System.currentTimeMillis());
    }

    private OfflineCause(long timeout, Throwable throwable, long time) {
        this.time = time;
        this.timeout = timeout;
        this.throwable = throwable;
    }

    void throwIfActual(DockerService dockerService) {
        if(isActual()) {
            throw new DockerException("Docker of " + dockerService.getId() + " is offline.");
        }
    }

    boolean isActual() {
        return this.time + this.timeout >= System.currentTimeMillis();
    }
}
