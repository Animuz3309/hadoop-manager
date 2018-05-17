package edu.scut.cs.hm.admin.component;

import edu.scut.cs.hm.admin.web.api.EventApi;
import edu.scut.cs.hm.admin.web.model.msg.UiAddSubscription;
import edu.scut.cs.hm.common.mb.SmartConsumer;
import edu.scut.cs.hm.common.mb.Subscriptions;
import edu.scut.cs.hm.common.utils.Closeables;
import edu.scut.cs.hm.model.EventWithTime;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

/**
 * Bean which hold subscriptions of session
 */
@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Scope(value = "websocket", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SessionSubscriptions implements AutoCloseable {

    private final ConcurrentMap<String, AutoCloseable> subs = new ConcurrentHashMap<>();
    private final Stomp stomp;

    public Collection<String> getIds() {
        ArrayList<String> list = new ArrayList<>(subs.keySet());
        list.sort(null);
        return Collections.unmodifiableList(list);
    }

    @Override
    public void close() throws Exception {
        subs.values().forEach(Closeables::close);
    }

    /**
     * Subscribe current session to specified {@link Subscriptions}
     * @param uas it not ever same as {@link Subscriptions#getId()}
     * @param subscriptions
     */
    public <T> void subscribe(UiAddSubscription uas, Subscriptions<T> subscriptions) {
        subs.computeIfAbsent(uas.getSource(), (i) -> subscriptions.openSubscription(new ConsumerImpl<T>(uas)));
        fire();
    }

    /**
     * remove and close specified subscription
     * @param id
     */
    public void unsubscribe(String id) {
        AutoCloseable subs = this.subs.remove(id);
        Closeables.close(subs);
        fire();
    }

    private void fire() {
        //send into our session info about changes
        stomp.sendToSession(EventApi.SUBSCRIPTIONS_GET, getIds());
    }

    private class ConsumerImpl<T> implements SmartConsumer<T>, AutoCloseable {
        private final String id;
        private final AtomicBoolean closed = new AtomicBoolean();
        private final long historySince;
        private final int historyCount;

        ConsumerImpl(UiAddSubscription uas) {
            this.id = uas.getSource();
            this.historyCount = uas.getHistoryCount();
            Date historySince = uas.getHistorySince();
            this.historySince = historySince == null? Long.MIN_VALUE : historySince.getTime();
        }

        @Override
        public void accept(T e) {
            stomp.sendToSession(id, e);
        }

        @Override
        public void close() throws Exception {
            //this flag prevent recursion
            if(!closed.compareAndSet(false, true)) {
                return;
            }
            unsubscribe(id);
        }

        @Override
        public Predicate<T> historyFilter() {
            return (event) -> {
                if(!(event instanceof EventWithTime)) {
                    return true;
                }
                return ((EventWithTime)event).getTimeInMilliseconds() >= historySince;
            };
        }

        @Override
        public int getHistoryCount() {
            return historyCount;
        }
    }
}
