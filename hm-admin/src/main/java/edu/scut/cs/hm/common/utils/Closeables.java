package edu.scut.cs.hm.common.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * Closeable utils to close class implement {@link AutoCloseable}
 */
@Slf4j
public final class Closeables {
    private Closeables() {}

    /**
     * silently close closeable, catch any exception and write it to log
     * @param autoCloseable or null
     */
    public static void close(AutoCloseable autoCloseable) {
        if(autoCloseable == null) {
            return;
        }
        try {
            autoCloseable.close();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("On close.", e);
        }
    }

    /**
     * Close specified object if it is instance of AutoCloseable
     * @param mayBeCloseable
     */
    public static void closeIfCloseable(Object mayBeCloseable) {
        if(mayBeCloseable instanceof AutoCloseable) {
            close((AutoCloseable) mayBeCloseable);
        }
    }

    /**
     * Apply {@link #close(AutoCloseable)} to each items in collection.
     * @param closeables any iterable or null
     */
    public static void closeAll(Iterable<? extends AutoCloseable> closeables) {
        if(closeables == null) {
            return;
        }
        closeables.forEach(Closeables::close);
    }
}
