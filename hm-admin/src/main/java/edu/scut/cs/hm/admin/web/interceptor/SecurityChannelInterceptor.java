package edu.scut.cs.hm.admin.web.interceptor;

import edu.scut.cs.hm.admin.security.TempAuth;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class SecurityChannelInterceptor implements ExecutorChannelInterceptor {
    private static final String AUTH_KEY = TempAuth.class.getName();

    @Override
    public Message<?> beforeHandle(Message<?> message, MessageChannel messageChannel, MessageHandler messageHandler) {
        SimpMessageHeaderAccessor smha = SimpMessageHeaderAccessor.wrap(message);
        Principal user = smha.getUser();
        if(user instanceof Authentication) {
            TempAuth auth = TempAuth.open((Authentication)user);
            smha.setHeader(AUTH_KEY, auth);
        }
        return message;
    }

    @Override
    public void afterMessageHandled(Message<?> message, MessageChannel messageChannel, MessageHandler messageHandler, Exception e) {
        SimpMessageHeaderAccessor smha = SimpMessageHeaderAccessor.wrap(message);
        Object attribute = smha.getHeader(AUTH_KEY);
        if(attribute instanceof TempAuth) {
            ((TempAuth)attribute).close();
        }
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel messageChannel) {
        return null;
    }

    @Override
    public void postSend(Message<?> message, MessageChannel messageChannel, boolean b) {

    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel messageChannel, boolean b, Exception e) {

    }

    @Override
    public boolean preReceive(MessageChannel messageChannel) {
        return false;
    }

    @Override
    public Message<?> postReceive(Message<?> message, MessageChannel messageChannel) {
        return null;
    }

    @Override
    public void afterReceiveCompletion(Message<?> message, MessageChannel messageChannel, Exception e) {

    }
}
