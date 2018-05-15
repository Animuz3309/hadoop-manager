package edu.scut.cs.hm.common.mb;

import edu.scut.cs.hm.common.mb.MessageBus;
import edu.scut.cs.hm.common.mb.MessageBusImpl;
import edu.scut.cs.hm.common.mb.MessageSubscriptionsWrapper;
import edu.scut.cs.hm.common.mb.Subscriptions;

public final class MessageBuses {

    private MessageBuses() {
    }

    /**
     * Create new instance of message bus with default exception handler.
     * @param id
     * @param type
     * @param <M>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <M> MessageBus<M> create(String id, Class<M> type) {
        return MessageBusImpl.<M, Subscriptions<M>>builder(type, MessageSubscriptionsWrapper::new).id(id).build();
    }

}
