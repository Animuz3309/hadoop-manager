package edu.scut.cs.hm.docker.mng;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import edu.scut.cs.hm.docker.res.ProcessEvent;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.function.Consumer;

@Slf4j
public class ProcessEventProcessor implements ResponseStreamProcessor<ProcessEvent> {

    @Override
    public void processResponseStream(StreamContext<ProcessEvent> context) {
        Consumer<ProcessEvent> watcher = context.getWatcher();
        InputStream response = context.getStream();
        SettableFuture<Boolean> interrupter = context.getInterrupter();
        interrupter.addListener(() -> Thread.currentThread().interrupt(), MoreExecutors.directExecutor());
        try (FrameReader frameReader = new FrameReader(response)) {

            Frame frame = frameReader.readFrame();
            while (frame != null && !interrupter.isDone()) {
                try {
                    ProcessEvent.watchRaw(watcher, frame.getMessage(), false);
                } catch (Exception e) {
                    log.error("Cannot read body", e);
                } finally {
                    frame = frameReader.readFrame();
                }
            }
        } catch (Exception t) {
            log.error("Cannot close reader", t);
        }

    }
}
