package edu.scut.cs.hm.admin.component;

import edu.scut.cs.hm.common.mb.MessageBus;
import edu.scut.cs.hm.common.mb.MessageBuses;
import edu.scut.cs.hm.common.mb.Subscription;
import edu.scut.cs.hm.common.mb.Subscriptions;
import edu.scut.cs.hm.common.utils.Closeables;
import edu.scut.cs.hm.model.Severity;
import edu.scut.cs.hm.model.WithSeverity;
import edu.scut.cs.hm.model.event.EventsUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ErrorAggregator implements AutoCloseable, InitializingBean {
    // we must store events for last 24 hours, but our queue does not support limit by time
    private static final int MAX_SIZE = 1024 * 10;
    private final MessageBus<WithSeverity> bus;
    private final List<AutoCloseable> closeables = new ArrayList<>();
    private final ListableBeanFactory beanFactory;

    public ErrorAggregator(PersistentBusFactory pbf, ListableBeanFactory beanFactory) {
        this.bus = pbf.create(WithSeverity.class, EventsUtils.BUS_ERRORS, MAX_SIZE);
        this.beanFactory = beanFactory;
    }

    public Subscriptions<WithSeverity> getSubscriptions() {
        return bus.asSubscriptions();
    }

    @Override
    public void close() throws Exception {
        this.closeables.forEach(Closeables::close);
        this.closeables.clear();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String[] beanNames = beanFactory.getBeanNamesForType(Subscriptions.class, false, false);
        for(String beanName: beanNames) {
            if(EventsUtils.BUS_ERRORS.equals(beanName)) {
                continue;
            }
            Subscriptions<?> bean = beanFactory.getBean(beanName, Subscriptions.class);
            Subscription subs = bean.openSubscription(this::onEvent);
            this.closeables.add(subs);
        }
    }

    private void onEvent(Object o) {
        if(!(o instanceof WithSeverity)) {
            return;
        }
        WithSeverity ws = (WithSeverity) o;
        Severity severity = ws.getSeverity();
        if(severity != Severity.ERROR && severity != Severity.WARNING) {
            return;
        }
        this.bus.accept(ws);
    }
}
