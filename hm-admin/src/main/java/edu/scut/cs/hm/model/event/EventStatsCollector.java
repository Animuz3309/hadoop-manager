package edu.scut.cs.hm.model.event;

import edu.scut.cs.hm.common.mb.MessageBus;
import edu.scut.cs.hm.common.mb.MessageBusImpl;
import edu.scut.cs.hm.common.mb.MessageSubscriptionsWrapper;
import edu.scut.cs.hm.common.mb.Subscriptions;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class EventStatsCollector<E> implements Consumer<E>, AutoCloseable {

    private class Bag {
        private final Object lock = new Object();
        private final Object key;
        private int count = 0;
        private E last;

        Bag(Object key) {
            this.key = key;
        }

        void accept(E e) {
            EventStats<E> stats;
            synchronized (lock) {
                count++;
                last = e;
                stats = makeEvent();
            }
            bus.accept(stats);
        }

        private EventStats<E> makeEvent() {
            synchronized (lock) {
                return new EventStats<>(key, last, count);
            }
        }
    }

    private final ConcurrentMap<Object, Bag> bags = new ConcurrentHashMap<>();
    private final MessageBus<EventStats<E>> bus;
    private final Function<E, Object> keyFactory;

    @SuppressWarnings("unchecked")
    public EventStatsCollector(String busId, Function<E, Object> keyFactory) {
        this.keyFactory = keyFactory;
        Class<EventStats<E>> type = (Class) EventStats.class;
        this.bus = MessageBusImpl.builder(type, MessageSubscriptionsWrapper::new)
                .id(busId)
                .onSubscribe(this::onSubscribe)
                .build();
    }

    private void onSubscribe(MessageBus<EventStats<E>> messageBus, Consumer<EventStats<E>> consumer) {
        bags.forEach((k, b) -> {
            consumer.accept(b.makeEvent());
        });
    }

    @Override
    public void accept(E e) {
        Object key = keyFactory.apply(e);
        if(key == null) {
            return;
        }
        Bag bag = bags.computeIfAbsent(key, Bag::new);
        bag.accept(e);
    }

    public Subscriptions<EventStats<E>> getSubscriptions() {
        return bus.asSubscriptions();
    }

    public String getBusId() {
        return this.bus.getId();
    }


    @Override
    public void close() throws Exception {
        this.bus.close();
    }
}
