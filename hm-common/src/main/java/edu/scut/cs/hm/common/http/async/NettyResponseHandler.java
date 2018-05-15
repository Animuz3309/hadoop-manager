package edu.scut.cs.hm.common.http.async;

import io.netty.buffer.ByteBufHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.concurrent.SettableListenableFuture;

class NettyResponseHandler extends SimpleChannelInboundHandler<HttpObject> {

    private final SettableListenableFuture<ClientHttpResponse> responseFuture;
    private final ChunkedInputStream<ByteBufHolder> in = new ChunkedInputStream<>(ByteBufHolderAdapter.INSTANCE);

    NettyResponseHandler(SettableListenableFuture<ClientHttpResponse> responseFuture) {
        this.responseFuture = responseFuture;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, HttpObject response) throws Exception {
        if(response instanceof HttpResponse) {
            this.responseFuture.set(new NettyResponse(context, (HttpResponse) response, in));
        } else if(response instanceof HttpContent) {
            HttpContent cont = (HttpContent) response;
            in.add(cont);
            if(response instanceof LastHttpContent) {
                in.end();
            }
        } else {
            throw new RuntimeException("Unknown message: " + response);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) throws Exception {
        this.responseFuture.setException(cause);
    }

}
