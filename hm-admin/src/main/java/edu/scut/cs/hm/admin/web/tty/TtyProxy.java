package edu.scut.cs.hm.admin.web.tty;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import edu.scut.cs.hm.common.utils.Closeables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.*;

import java.util.Map;

/**
 * It is a proxy to deliver message from backend to frontend, and has the method to send message to backend
 */
@Slf4j
class TtyProxy implements WebSocketHandler {

    private static final String KEY = TtyProxy.class.getName();
    private static final Joiner.MapJoiner DIJOINER = Joiner.on("\n\r").withKeyValueSeparator(" = ");
    private final String containerId;
    private final WebSocketSession frontend;
    private final Map<String, String> diagnosticInfo;
    private volatile WebSocketSession backend;

    TtyProxy(String containerId, WebSocketSession frontend, Map<String, String> diagnosticInfo) {
        this.containerId = containerId;
        this.frontend = frontend;
        this.diagnosticInfo = diagnosticInfo;
    }

    /**
     * Store the TtyProxy in frontend WebSocket session
     * @param session
     * @param ttyProxy
     */
    static void set(WebSocketSession session, TtyProxy ttyProxy) {
        session.getAttributes().put(KEY, ttyProxy);
    }

    /**
     * Retrieve the TtyProxy in frontend WebSocket session
     * @param session
     * @return
     */
    static TtyProxy get(WebSocketSession session) {
        return (TtyProxy) session.getAttributes().get(KEY);
    }

    private void closeCausedBack() {
        // split close method for easy resolve cause of closing
        log.info("Close caused back of {}", this);
        close();
    }

    void closeCausedFront() {
        // split close method for easy resolve cause of closing
        log.info("Close caused front of {}", this);
        close();
    }

    private void close() {
        frontend.getAttributes().remove(KEY, this);
        // usually this method called twice - at close frontend, and at close backend
        Closeables.close(backend);
        Closeables.close(frontend);
    }

    /**
     * Send message to backend WebSocket session
     * @param message
     */
    void toBackend(WebSocketMessage<?> message) {
        WebSocketSession localBackend = this.backend;
        if (localBackend == null) {
            return;
        }
        try {
            if (!localBackend.isOpen()) {
                log.warn("Message to closed backend, {}", this);
                closeCausedBack();
                return;
            }
            localBackend.sendMessage(message);
        } catch (Exception e) {
            log.error("In {}, can no send message to backend due to: ", this, e);
        }
    }

    /**
     * After connection established to backend WebSocket session
     * @param backendSession
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession backendSession) throws Exception {
        this.backend = backendSession;
        log.info("Success connect to backed with sessions: front={}, back={}", frontend, backendSession);
        // below we sent some diagnostic info to frontend only
        send(frontend, "Connect to container: " + containerId + "\n\r " + DIJOINER.join(diagnosticInfo.entrySet()) + "\n\r");
    }

    private void send(WebSocketSession session, String msg) {
        try {
            session.sendMessage(new TextMessage(msg));
        } catch (Exception e) {
            log.error("Can not send message to {} due to error:", session, e);
        }
    }

    /**
     * Receive message from backend WebSocket session, and then deliver to frontend session
     * @param backendSession
     * @param message
     * @throws Exception
     */
    @Override
    public void handleMessage(WebSocketSession backendSession, WebSocketMessage<?> message) throws Exception {
        try {
            if(!frontend.isOpen()) {
                log.warn("Message to closed frontend, {}", this);
                closeCausedFront();
                return;
            }
            frontend.sendMessage(message);
        } catch (Exception e) {
            log.error("In {}, can no send message to frontend due to: ", this, e);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) throws Exception {
        log.error("Backend transport error:", throwable);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) throws Exception {
        closeCausedBack();
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("containerId", containerId)
                .add("frontend", frontend)
                .add("backend", backend)
                .toString();
    }
}
