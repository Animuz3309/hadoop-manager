package edu.scut.cs.hm.agent.proxy;

import edu.scut.cs.hm.common.utils.Closeables;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.util.ReferenceCountUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * WebSocket proxy to connect others client outside to docker.sock in this computer
 */
@Slf4j
@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class WsProxy extends Endpoint {

    // sock("/var/run/docker.sock")
    private final Backend backend;

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        String id = session.getId();
        log.debug("{}: open ws proxy", id);
        try {
            ChannelFuture cf = backend.connect().sync();
            Channel channel = cf.channel();

            WebSocketClientProtocolHandler wscph = makeWsProtocolHandler(session);
            WebSocketClientHandshaker handshaker = wscph.handshaker();
            WsHandler handler = new WsHandler(handshaker, channel, session);
            channel.pipeline().addLast(new HttpObjectAggregator(1024 * 4),
                    WebSocketClientCompressionHandler.INSTANCE,
                    wscph,
                    handler);
            handshaker.handshake(channel);
            log.debug("{}: wait messages", id);

            session.addMessageHandler(String.class, handler::onFrontString);
            session.addMessageHandler(ByteBuffer.class, handler::onFrontBytes);
        } catch (Exception e) {
            log.error("{}: can not establish ws connect with backed", id, e);
        }
    }

    private WebSocketClientProtocolHandler makeWsProtocolHandler(Session session) {
        WebSocketVersion version = ProxyUtils.getWsVersion(session.getProtocolVersion());
        WebSocketClientHandshaker wshs = WebSocketClientHandshakerFactory.newHandshaker(
                session.getRequestURI(), version, null, true, new DefaultHttpHeaders());
        return new WebSocketClientProtocolHandler(wshs);
    }

    private static class WsHandler extends ChannelInboundHandlerAdapter {
        private final String id;
        private final Session session;
        private final WebSocketClientHandshaker handshaker;
        private final Channel channel;

        WsHandler(WebSocketClientHandshaker handshaker,
                         Channel channel,
                         Session session) {
            this.id = session.getId();
            this.handshaker = handshaker;
            this.channel = channel;
            this.session = session;
        }

        /**
         * Get message from sock("/var/run/docker.sock") and then send back to ws session
         * @param ctx
         * @param msg
         * @throws Exception
         */
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            try {
                Channel ch = ctx.channel();
                if (!handshaker.isHandshakeComplete()) {
                    handshaker.finishHandshake(ch, (FullHttpResponse) msg);
                    log.info("WebSocket Client connected!");
                    return;
                }

                if (msg instanceof FullHttpResponse) {
                    FullHttpResponse response = (FullHttpResponse) msg;
                    throw new IllegalStateException(
                            "Unexpected FullHttpResponse (getStatus=" + response.status() +
                                    ", content=" + response.content().toString(StandardCharsets.UTF_8) + ')');
                }

                if(!(msg instanceof WebSocketFrame)) {
                    return;
                }

                if(!session.isOpen()) {
                    log.debug("{}: front session is closed", id);
                    internalClose();
                    return;
                }
                RemoteEndpoint.Async ar = session.getAsyncRemote();
                if (msg instanceof TextWebSocketFrame) {
                    ar.sendText(((TextWebSocketFrame)msg).text(), this::handleSend);
                } else if(msg instanceof BinaryWebSocketFrame) {
                    ByteBuf content = ((BinaryWebSocketFrame) msg).content();
                    //we need to make copy because async send may use buffer out of its lifecycle
                    ar.sendBinary(copy(content), this::handleSend);
                }
            } finally {
                ReferenceCountUtil.release(msg);
            }
        }

        private ByteBuffer copy(ByteBuf content) {
            ByteBuffer tmp = ByteBuffer.allocate(content.readableBytes());
            content.readBytes(tmp);
            tmp.flip();
            return tmp;
        }

        private void handleSend(SendResult res) {
            Throwable th = res.getException();
            if (th != null) {
                log.error("{}: error on send event to front, close ", id, th);
                internalClose();
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            log.error("{}: error in ws handler, close.", id, cause);
            internalClose();
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            log.debug("{}: ws handler unregistered, close.", id);
            internalClose();
        }

        private void internalClose() {
            log.debug("{}: closing ws proxy", id);
            Channel tmp = this.channel;
            if(tmp != null) {
                Closeables.close(tmp::close);
            }
            Closeables.close(session);
        }

        private void onFrontString(String str) {
            TextWebSocketFrame msg = new TextWebSocketFrame(str);
            channel.writeAndFlush(msg);
        }

        private void onFrontBytes(ByteBuffer bb) {
            BinaryWebSocketFrame bf = new BinaryWebSocketFrame();
            bf.content().writeBytes(bb);
            channel.writeAndFlush(bf);
        }
    }
}
