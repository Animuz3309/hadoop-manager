package edu.scut.cs.hm.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Throwable helpers
 */
@Slf4j
public final class Throwables {
    private Throwables() {}

    /**
     * Print specified throwable to string. If throwable is null, then return null.
     * @param e
     * @return
     */
    public static String printToString(Throwable e) {
        if(e == null) {
            return null;
        }
        String res = null;
        try (StringWriter stringWriter = new StringWriter()) {
            e.printStackTrace(new PrintWriter(stringWriter));
            res = stringWriter.toString();
        } catch (IOException ioe) {
            // usually this code used for processing catched throwables
            //   therefore we don't need another annoying exception
        }
        return res;
    }

    /**
     * Wrap generic exception to RuntimeException, or cast and return it.
     * @param e
     * @return
     */
    public static RuntimeException asRuntime(Throwable e) {
        if(e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new RuntimeException(e);
    }

    /**
     * Find in chain of causes first instance of specified type.
     * @param e
     * @param type
     * @return instance of specified type or null.
     */
    public static <T extends Throwable> T find(Throwable e, Class<T> type) {
        Assert.notNull(e, "throwable can't be null");
        Assert.notNull(type, "type can't be null");
        while(e != null) {
            if(type.isInstance(e)) {
                return type.cast(e);
            }
            e = e.getCause();
        }
        return null;
    }

    /**
     * Test that specified throwable has specified type in chain of causes.
     * @param e
     * @param type
     * @return true if throwable has specified type in chain of causes.
     */
    public static boolean has(Throwable e, Class<? extends Throwable> type) {
        Assert.notNull(e, "throwable can't be null");
        Assert.notNull(type, "type can't be null");
        while(e != null) {
            if(type.isInstance(e)) {
                return true;
            }
            e = e.getCause();
        }
        return false;
    }

    public static Thread.UncaughtExceptionHandler uncaughtHandler() {
        return uncaughtHandler(log);
    }

    public static Thread.UncaughtExceptionHandler uncaughtHandler(Logger log) {
        return uncaughtHandler(log, "Uncaught exception.");
    }

    public static Thread.UncaughtExceptionHandler uncaughtHandler(Logger log, String msg) {
        return (thread, ex) -> log.error(msg, ex);
    }
}
