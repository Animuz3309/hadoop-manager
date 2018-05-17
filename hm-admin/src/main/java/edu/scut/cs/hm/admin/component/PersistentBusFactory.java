package edu.scut.cs.hm.admin.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.scut.cs.hm.common.fc.FbJacksonAdapter;
import edu.scut.cs.hm.common.fc.FbQueue;
import edu.scut.cs.hm.common.fc.FbStorage;
import edu.scut.cs.hm.common.mb.*;
import edu.scut.cs.hm.common.utils.Closeables;
import edu.scut.cs.hm.common.utils.Key;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Slf4j
@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class PersistentBusFactory implements InitializingBean, DisposableBean {
    /**
     * Key of bus extension
     * @see MessageBus#getExtension(Key)
     */
    @SuppressWarnings("unchecked")
    public static final Key<PersistentBus<?>> EXT_KEY = new Key<>((Class)PersistentBus.class);

    public class PersistentBus<T> implements AutoCloseable {
        private final Consumer<T> queueListener;
        private boolean closed;
        private final FbQueue<T> queue;
        private final MessageBusImpl<T, MessageSubscriptionsWrapper<T>> bus;

        PersistentBus(Class<T> type, String id, int size) {
            this.queue = FbQueue.builder(new FbJacksonAdapter<>(objectMapper, type))
                    .id(id)
                    .storage(fbStorage)
                    .maxSize(size)
                    .build();
            this.queueListener = queue::push;
            this.bus = MessageBusImpl
                    .builder(type, MessageSubscriptionsWrapper::new)
                    .id(id)
                    .onSubscribe(this::flusher)
                    .build();
            this.bus.getOrCreateExtension(EXT_KEY, (k, b) -> this);
            this.bus.subscribe(queueListener);
        }

        private void flusher(MessageBus<T> mb, Consumer<T> l) {
            if(WrappedConsumer.unwrap(l) == queueListener) {
                return;
            }
            SmartConsumer<T> sc = SmartConsumer.of(l);
            int historyCount = sc.getHistoryCount();
            if(historyCount == 0) {
                return;
            }
            Predicate<T> filter = sc.historyFilter();
            try {
                Iterator<T> iter = queue.iterator(historyCount);
                while (iter.hasNext()) {
                    T next = iter.next();
                    if(filter.test(next)) {
                        l.accept(next);
                    }
                }
            } catch (Exception e) {
                log.error("Can not up history to new consumer, due to error.", e);
            }
        }

        @Override
        public void close() throws Exception {
            if(closed) {
                // prevent SOE, because this method may be invoked from bus
                return;
            }
            closed = true;
            this.bus.close();
            this.queue.close();
        }

        public MessageBus<T> getBus() {
            return bus;
        }

        public FbQueue<T> getQueue() {
            return queue;
        }
    }

    private final ObjectMapper objectMapper;
    private final FbStorage fbStorage;
    private final ConcurrentMap<String, PersistentBus<?>> map = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> MessageBus<T> create(Class<T> type, String id, int size) {
        PersistentBus<T> entry = (PersistentBus<T>) map.computeIfAbsent(id, (i) -> new PersistentBus<>(type, id, size));
        return entry.getBus();
    }

    public PersistentBus<?> get(String id) {
        return map.get(id);
    }

    @Override
    public void destroy() throws Exception {
        map.values().forEach(Closeables::close);
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
