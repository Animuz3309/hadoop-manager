#! /usr/bin/env groovy
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.DefaultHttpHeaders
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http.HttpClientCodec
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.websocketx.*

import java.nio.ByteBuffer
import java.nio.charset.Charset

import static io.netty.handler.codec.http.websocketx.WebSocketVersion.V13

def cons = System.console() ?: System.in.newReader()
def container
container = cons.readLine()

def uri = "ws://localhost:8771/containers/$container/attach/ws?stream=1&stdin=1&stdout=1&stderr=1".toURI()
run(uri, cons)

def run(uri, cons) {
    def b = new Bootstrap()
    def group = new NioEventLoopGroup()
    try {
        def final handler = createHandler(uri)
        b.group(group)
        .channel(NioSocketChannel.class)
        .handler({ ch ->
            ch.pipeline().addLast(
                    new HttpClientCodec(),
                    new HttpObjectAggregator(8192),
                    handler) } as ChannelInitializer<SocketChannel>)
        Channel ch = b.connect(uri.getHost(), uri.getPort()).sync().channel()
        handler.handsharkerFuture.sync()

        while (true) {
            String msg = cons.readLine()
            if ('bye' == msg.toLowerCase()) {
                ch.writeAndFlush(new CloseWebSocketFrame())
                ch.closeFuture().sync()
                break
            } else if ('ping' == msg.toLowerCase()) {
                ch.writeAndFlush(new PingWebSocketFrame(Unpooled.wrappedBuffer([8, 1, 8, 1] as byte[])))
            } else {
                ch.writeAndFlush(new TextWebSocketFrame("$msg\n"))
            }
        }
    } finally {
        group.shutdownGracefully()
    }
}

def createHandler(URI uri) {
    new WebSocketClientHandler(
            WebSocketClientHandshakerFactory.newHandshaker(
                    uri, V13, null, false, new DefaultHttpHeaders()))
}

class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {
    final WebSocketClientHandshaker handshaker
    ChannelPromise handsharkerFuture

    WebSocketClientHandler(handshaker) {
        this.handshaker = handshaker
    }

    @Override
    void handlerAdded(ChannelHandlerContext ctx) {
        handsharkerFuture = ctx.newPromise()
    }

    @Override
    void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel())
    }

    @Override
    void channelInactive(ChannelHandlerContext ctx) {
        println 'WebSocket client disconnected'
    }

    @Override
    void channelRead0(ChannelHandlerContext ctx, Object msg) { Channel ch = ctx.channel()
        if (!handshaker.isHandshakeComplete()) {
            try {
                handshaker.finishHandshake(ch, msg as FullHttpResponse)
                println 'WebSocket client connected'
                handsharkerFuture.setSuccess()
            } catch (e) {
                println 'WebSocket client fail to connected'
                handsharkerFuture.setFailure(e)
            }
            return
        }

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse resp = msg as FullHttpResponse
            throw new IllegalStateException("Unexpected FullHttpResponse (status=${resp.status()}, content=${resp.content()})")
        }

        WebSocketFrame frame = msg as WebSocketFrame
        if (frame instanceof TextWebSocketFrame)
            println "WebSocket client received message: ${(frame as TextWebSocketFrame).text()}"
        else if (frame instanceof PongWebSocketFrame)
            println "WebSocket client received pong"
        else if (frame instanceof CloseWebSocketFrame) {
            println "WebSocket client received closing"
            ch.close()
        } else if(msg instanceof BinaryWebSocketFrame) {
            ByteBuf content = ((BinaryWebSocketFrame) msg).content()
            ByteBuffer tmp = ByteBuffer.allocate(content.readableBytes())
            content.readBytes(tmp)
            tmp.flip()
            println(Charset.forName('UTF-8').newDecoder().decode(tmp.asReadOnlyBuffer()))
        }
    }

    @Override
    void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace()
        if (!handsharkerFuture.done)
            handsharkerFuture.setFailure(cause)
        ctx.close()
    }
}