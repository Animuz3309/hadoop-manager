package edu.scut.cs.hm.admin.web.api;

import edu.scut.cs.hm.admin.component.EventSources;
import edu.scut.cs.hm.admin.component.FilterFactory;
import edu.scut.cs.hm.admin.component.PersistentBusFactory;
import edu.scut.cs.hm.admin.component.SessionSubscriptions;
import edu.scut.cs.hm.admin.web.model.error.UiError;
import edu.scut.cs.hm.admin.web.model.msg.UiAddSubscription;
import edu.scut.cs.hm.admin.web.model.msg.UiCountResult;
import edu.scut.cs.hm.common.fc.FbQueue;
import edu.scut.cs.hm.common.mb.Subscriptions;
import edu.scut.cs.hm.model.EventWithTime;
import edu.scut.cs.hm.model.ExtendedAssert;
import edu.scut.cs.hm.model.event.EventFilter;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(value = "/api/events/", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class EventApi {
    public static final String SUBSCRIPTIONS_GET = "/subscriptions/get";
    private final SessionSubscriptions subscriptions;
    private final EventSources sources;
    private final FilterFactory filterFactory;

    @MessageMapping(SUBSCRIPTIONS_GET)
    @SendToUser(broadcast = false)
    public Collection<String> listSubs() {
        return subscriptions.getIds();
    }

    @MessageMapping("/subscriptions/add")
    /* do no use List<> here due it not support deserialization from string, which is need for back capability */
    public void addSub(UiAddSubscription[] uases) {
        //we save absent subscriptions into array for prevent multiple log records
        List<String> absent = new ArrayList<>(0/*usually is no absent ids*/);
        for(UiAddSubscription uas: uases) {
            String id = uas.getSource();
            Subscriptions<?> subs = sources.get(id);
            if(subs == null) {
                absent.add(id);
                continue;
            }
            subscriptions.subscribe(uas, subs);
        }
        if(!absent.isEmpty()) {
            log.warn("Can not find subscriptions with ids: {}", absent);
        }
    }

    @MessageMapping("/subscriptions/del")
    public void delSub(List<String> ids) {
        ids.forEach(subscriptions::unsubscribe);
    }

    @MessageMapping("/subscriptions/available")
    @SendToUser(broadcast = false)
    public Collection<String> listAvailable() {
        return sources.list();
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Collection<String> listAvailableRest() {
        return sources.list();
    }

    @MessageExceptionHandler
    public UiError onException(Message message, Exception ex) {
        log.error("On message: {}" , SimpMessageHeaderAccessor.wrap(message).getDestination(), message, ex);
        return UiError.from(ex);
    }

    @ApiOperation("Count of elements in specified events source since specified time (24 hours by default)." +
            " Note that not all sources have persisted store, these sources do not support getting count.")
    @RequestMapping(value = "/{source:.*}/count", method = RequestMethod.GET)
    public UiCountResult countOfLastEvents(@PathVariable("source") String source,
                                           @RequestParam(name = "filter", required = false) List<String> filtersSrc,
                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                           @RequestParam(name = "from", required = false) LocalDateTime from) {
        Subscriptions<?> subs = sources.get(source);
        ExtendedAssert.notFound(subs, "Can not find Subscriptions: '" + source + "'");
        PersistentBusFactory.PersistentBus<?> pb = subs.getExtension(PersistentBusFactory.EXT_KEY);
        ExtendedAssert.notFound(pb, "Can not find persisted queue: '" + source + "'");
        List<EventFilter> collectors = new ArrayList<>();
        if(filtersSrc != null) {
            filtersSrc.forEach((src) -> collectors.add(new EventFilter(filterFactory, src)));
        }
        if(from == null) {
            from = LocalDateTime.now().minusDays(1);
        }
        long fromMillis = from.toEpochSecond(ZoneOffset.UTC);
        FbQueue<?> q = pb.getQueue();
        Iterator<?> iter = q.iterator();
        int i = 0;
        while(iter.hasNext()) {
            Object next = iter.next();
            if(!(next instanceof EventWithTime)) {
                continue;
            }
            long millis = ((EventWithTime) next).getTimeInMilliseconds();
            if(millis < fromMillis) {
                continue;
            }
            collectors.forEach(fc -> fc.collect(next));
            i++;
        }
        UiCountResult res = new UiCountResult();
        res.setFiltered(collectors.stream().map(EventFilter::toUi).collect(Collectors.toList()));
        res.setSource(source);
        res.setCount(i);
        res.setFrom(from);
        return res;
    }
}
