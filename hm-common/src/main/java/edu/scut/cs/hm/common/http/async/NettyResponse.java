package edu.scut.cs.hm.common.http.async;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * We create our implementation based on {@link org.springframework.http.client.Netty4ClientHttpResponse }
 * due to need consume of endless stream with "TransferEncoding: chunked", which default implementation does not allow.
 */
class NettyResponse implements ClientHttpResponse {
    private final ChannelHandlerContext context;

    private final HttpResponse nettyResponse;

    private final InputStream body;

    private volatile HttpHeaders headers;


    NettyResponse(ChannelHandlerContext context, HttpResponse nettyResponse, InputStream body) {
        Assert.notNull(context, "ChannelHandlerContext must not be null");
        Assert.notNull(nettyResponse, "FullHttpResponse must not be null");
        this.context = context;
        this.nettyResponse = nettyResponse;
        this.body = body;
    }

    @Override
    public HttpStatus getStatusCode() throws IOException {
        return HttpStatus.valueOf(getRawStatusCode());
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getRawStatusCode() throws IOException {
        return this.nettyResponse.getStatus().code();
    }

    @Override
    @SuppressWarnings("deprecation")
    public String getStatusText() throws IOException {
        return this.nettyResponse.getStatus().reasonPhrase();
    }

    @Override
    public HttpHeaders getHeaders() {
        if (this.headers == null) {
            HttpHeaders headers = new HttpHeaders();
            for (Map.Entry<String, String> entry : this.nettyResponse.headers()) {
                headers.add(entry.getKey(), entry.getValue());
            }
            this.headers = headers;
        }
        return this.headers;
    }

    @Override
    public InputStream getBody() throws IOException {
        return this.body;
    }

    @Override
    public void close() {
        this.context.close();
    }

}