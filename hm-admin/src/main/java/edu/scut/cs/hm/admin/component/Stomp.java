package edu.scut.cs.hm.admin.component;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.SimpAttributes;
import org.springframework.messaging.simp.SimpAttributesContextHolder;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.MimeTypeUtils;

@Slf4j
@Component
@Scope(value = "websocket" /* do not proxy this object, because it disallow to use it in other threads */)
public class Stomp {

    @Getter
    private final String sessionId;
    private final SimpMessagingTemplate template;
    private final MessageChannel clientChannel;

    @Autowired
    public Stomp(SimpMessagingTemplate template,
                 @Qualifier("clientOutboundChannel") MessageChannel clientChannel) {
        SimpAttributes sa = SimpAttributesContextHolder.currentAttributes();
        this.sessionId = sa.getSessionId();
        this.template = template;
        this.clientChannel = clientChannel;
    }

    public void sendToSubscription(String subscriptionId, String dest, Object msg) {
        Assert.notNull(subscriptionId, "subscriptionId is null");
        StompHeaderAccessor sha = createHeaders(sessionId, subscriptionId);
        MessageConverter messageConverter = this.template.getMessageConverter();
        sha.setDestination("/queue/" + dest);
        Message<?> message = messageConverter.toMessage(msg, sha.getMessageHeaders());
        clientChannel.send(message);
    }

    public void sendToSession(String dest, Object msg) {
        StompHeaderAccessor sha = StompHeaderAccessor.create(StompCommand.MESSAGE);
        sha.setLeaveMutable(true);
        sha.setSessionId(sessionId);
        this.template.convertAndSendToUser(sessionId, "/queue/" + dest, msg, sha.getMessageHeaders());
    }

    static StompHeaderAccessor createHeaders(String sessionId, String subscriptionId) {
        StompHeaderAccessor sha = StompHeaderAccessor.create(StompCommand.MESSAGE);
        sha.setLeaveMutable(true);
        sha.setMessageTypeIfNotSet(SimpMessageType.MESSAGE);
        sha.setSubscriptionId(subscriptionId);
        sha.setSessionId(sessionId);
        sha.setContentType(MimeTypeUtils.APPLICATION_JSON);
        return sha;
    }
}
