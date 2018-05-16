package edu.scut.cs.hm.admin.web.tty;

import com.google.common.collect.ImmutableMap;
import edu.scut.cs.hm.admin.security.TempAuth;
import edu.scut.cs.hm.admin.service.NodeStorage;
import edu.scut.cs.hm.common.utils.AddressUtils;
import edu.scut.cs.hm.docker.model.container.DockerContainer;
import edu.scut.cs.hm.model.ExtendedAssert;
import edu.scut.cs.hm.model.container.ContainerRegistration;
import edu.scut.cs.hm.model.container.ContainerStorage;
import edu.scut.cs.hm.model.node.NodeInfo;
import edu.scut.cs.hm.model.node.NodeRegistration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@Component
public class WsTtyHandler implements WebSocketHandler {

    private ContainerStorage containerStorage;
    private NodeStorage nodeStorage;
    private WebSocketClient webSocketClient;

    @Autowired
    public WsTtyHandler(ContainerStorage containerStorage,
                        NodeStorage nodeStorage,
                        WebSocketClient webSocketClient) {
        this.containerStorage = containerStorage;
        this.nodeStorage = nodeStorage;
        this.webSocketClient = webSocketClient;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession frontendSession) throws Exception {
        URI uri = frontendSession.getUri();
        try {
            UriComponents uc = UriComponentsBuilder.fromUri(uri).build();
            MultiValueMap<String, String> params = uc.getQueryParams();
            String containerId = params.getFirst("container");
            try(TempAuth ta = withAuth(frontendSession)) {
                connectToContainer(frontendSession, containerId);
            }
        } catch (Exception e) {
            log.error("Can not establish connection for '{}' due to error:", uri, e);
        }
    }

    private TempAuth withAuth(WebSocketSession session) {
        Authentication auth = (Authentication) session.getPrincipal();
        return TempAuth.open(auth);
    }

    private void connectToContainer(WebSocketSession frontedSession, String containerId) {
        ContainerRegistration containerReg = containerStorage.getContainer(containerId);
        ExtendedAssert.notFound(containerReg, "Can not find container: " + containerId);
        NodeRegistration nodeReg = nodeStorage.getNodeRegistration(containerReg.getNode());
        DockerContainer dc = containerReg.getContainer();
        NodeInfo nodeInfo = nodeReg.getNodeInfo();
        TtyProxy tty = new TtyProxy(containerReg.getId(), frontedSession, ImmutableMap.<String, String>builder()
                .put("container.name", dc.getName())
                .put("container.image", dc.getImage())
                .put("node.name", nodeInfo.getName())
                .put("node.addr", nodeInfo.getAddress())
                .build()
        );
        TtyProxy.set(frontedSession, tty);
        ListenableFuture<WebSocketSession> future = webSocketClient.doHandshake(tty, getContainerUri(containerReg.getId(), nodeReg));
        future.addCallback((r) -> {}, (e) -> log.error("failure to open backend connection to '{}' of cluster '{}' due to error: ", containerId, nodeReg.getCluster(), e));
    }

    private String getContainerUri(String containerId, NodeRegistration nr) {
        String addr = nr.getNodeInfo().getAddress();
        String host = AddressUtils.getHostPort(addr);
        String proto = AddressUtils.isHttps(addr)? "wss" : "ws";
        return proto + "://" + host + "/containers/" + containerId +
                "/attach/ws?stream=true&stdin=true&stdout=true&stderr=true";
    }

    @Override
    public void handleMessage(WebSocketSession frontedSession, WebSocketMessage<?> message) throws Exception {
        TtyProxy tty = TtyProxy.get(frontedSession);
        if(tty == null) {
            return;
        }
        tty.toBackend(message);
    }

    @Override
    public void handleTransportError(WebSocketSession frontedSession, Throwable throwable) throws Exception {
        log.error("Frontend transport error: ", throwable);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession frontedSession, CloseStatus closeStatus) throws Exception {
        TtyProxy tty = TtyProxy.get(frontedSession);
        if(tty != null) {
            tty.closeCausedFront();
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
