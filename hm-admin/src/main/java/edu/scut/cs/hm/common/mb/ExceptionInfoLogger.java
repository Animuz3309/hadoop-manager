package edu.scut.cs.hm.common.mb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Standard implementation of {@link ExceptionInfo } consumer
 */
public class ExceptionInfoLogger implements Consumer<ExceptionInfo> {

    private static final ExceptionInfoLogger INSTANCE = new ExceptionInfoLogger();
    private final Logger log = LoggerFactory.getLogger(getClass());

    private ExceptionInfoLogger() {
    }

    @Override
    public void accept(ExceptionInfo ei) {
        log.error("Exception in message bus '{}' from consumer '{}' on message '{}'",
                ei.getBus().getId(),
                ei.getConsumer(),
                ei.getMessage(),
                ei.getThrowable());
    }

    public static ExceptionInfoLogger getInstance() {
        return INSTANCE;
    }
}
