package edu.scut.cs.hm.docker.mng;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import edu.scut.cs.hm.common.utils.Throwables;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

@Slf4j
public class JsonStreamProcessor<T> implements ResponseStreamProcessor<T> {

    private static final JsonFactory JSON_FACTORY = new JsonFactory();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
    }

    private final Class<T> clazz;

    public JsonStreamProcessor(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void processResponseStream(StreamContext<T> context) {
        Consumer<T> watcher = context.getWatcher();
        InputStream response = context.getStream();
        final Thread thread = Thread.currentThread();
        SettableFuture<Boolean> interrupter = context.getInterrupter();
        interrupter.addListener(thread::interrupt, MoreExecutors.directExecutor());
        try {
            JsonParser jp = JSON_FACTORY.createParser(response);
            Boolean closed = jp.isClosed();
            JsonToken nextToken = jp.nextToken();
            while (!closed && nextToken != null && nextToken != JsonToken.END_OBJECT && !interrupter.isDone()) {
                try {
                    ObjectNode objectNode = OBJECT_MAPPER.readTree(jp);
                    // exclude empty item serialization into class #461
                    if (!objectNode.isEmpty(null)) {
                        T next = OBJECT_MAPPER.treeToValue(objectNode, clazz);
                        log.trace("Monitor value: {}", next);
                        watcher.accept(next);
                    }
                } catch (Exception e) {
                    log.error("Error on process json item.", e);
                }

                closed = jp.isClosed();
                nextToken = jp.nextToken();
            }
        } catch (Throwable t) {
            throw Throwables.asRuntime(t);
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                log.error("Can't close stream", e);

            }
        }

    }

}