package edu.scut.cs.hm.common.http.async;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.SocketChannelConfig;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.Data;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.AsyncClientHttpRequest;
import org.springframework.http.client.AsyncClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

@Data
@SuppressWarnings("deprecation")
public class NettyRequestFactory implements ClientHttpRequestFactory, AsyncClientHttpRequestFactory,
        InitializingBean, DisposableBean {

    private final EventLoopGroup eventLoopGroup;
    private final boolean defaultEventLoopGroup;

    /**
     * Set the SSL context. When configured it is used to create and insert an
     * {@link io.netty.handler.ssl.SslHandler} in the channel pipeline.
     * <p>By default this is not set.
     */
    private SslContext sslContext;
    private int connectTimeout = -1;

    /**
     * Set the underlying URLConnection's read timeout (in milliseconds).
     * A timeout value of 0 specifies an infinite timeout.
     * @see ReadTimeoutHandler
     */
    private int readTimeout = -1;
    private volatile Bootstrap bootstrap;

    /**
     * Create a new {@code Netty4ClientHttpRequestFactory} with a default
     * {@link NioEventLoopGroup}.
     */
    public NettyRequestFactory() {
        int ioWorkerCount = Runtime.getRuntime().availableProcessors() * 2;
        this.eventLoopGroup = new NioEventLoopGroup(ioWorkerCount);
        this.defaultEventLoopGroup = true;
    }

    private Bootstrap getBootstrap() {
        if (this.bootstrap == null) {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(this.eventLoopGroup).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            configureChannel(channel.config());
                            ChannelPipeline pipeline = channel.pipeline();
                            if (sslContext != null) {
                                pipeline.addLast(sslContext.newHandler(channel.alloc()));
                            }
                            pipeline.addLast(new HttpClientCodec());
                            //pipeline.addLast(new HttpObjectAggregator(maxResponseSize));
                            if (readTimeout > 0) {
                                pipeline.addLast(new ReadTimeoutHandler(readTimeout,
                                        TimeUnit.MILLISECONDS));
                            }
                        }
                    });
            this.bootstrap = bootstrap;
        }
        return this.bootstrap;
    }

    /**
     * Template method for changing properties on the given {@link SocketChannelConfig}.
     * <p>The default implementation sets the connect timeout based on the set property.
     * @param config the channel configuration
     */
    private void configureChannel(SocketChannelConfig config) {
        if (this.connectTimeout >= 0) {
            config.setConnectTimeoutMillis(this.connectTimeout);
        }
    }

    @Override
    public void afterPropertiesSet() {
        getBootstrap();
    }


    @Override
    public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
        return createRequestInternal(uri, httpMethod);
    }

    @Override
    public AsyncClientHttpRequest createAsyncRequest(URI uri, HttpMethod httpMethod) throws IOException {
        return createRequestInternal(uri, httpMethod);
    }

    private NettyRequest createRequestInternal(URI uri, HttpMethod httpMethod) {
        return new NettyRequest(getBootstrap(), uri, httpMethod);
    }


    @Override
    public void destroy() throws InterruptedException {
        if (this.defaultEventLoopGroup) {
            // Clean up the EventLoopGroup if we created it in the constructor
            this.eventLoopGroup.shutdownGracefully().sync();
        }
    }
}
