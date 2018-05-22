package edu.scut.cs.hm.agent.proxy;

import edu.scut.cs.hm.common.utils.Closeables;
import edu.scut.cs.hm.common.utils.UUIDs;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.http.HttpHeaders.TRANSFER_ENCODING;

@Slf4j
public class ProxyServlet extends GenericServlet {
    /**
     * Max len of frontend request content
     */
    private static final int MAX_FRONT_REQ_LEN = 1024 * 1024;

    @Autowired
    private Backend backend;

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;
        String id = UUIDs.longUid();
        NettyHandler handler = null;
        try {
            String uri = ProxyUtils.reconstructUri(request);
            log.debug("{}: start {} {}", id, request.getMethod(), uri);
            String upgrade = request.getHeader(org.springframework.http.HttpHeaders.UPGRADE);
            if (upgrade != null) {
                doUpgrade(id, request,  response);
                return;
            }
            ChannelFuture cf = backend.connect().sync();
            Channel channel = cf.channel();
            DefaultFullHttpRequest backendReq = buildRequest(id, request, uri);
            handler = new NettyHandler(id, request, response);
            channel.pipeline().addLast(handler);
            channel.writeAndFlush(backendReq).sync();
            channel.closeFuture().sync();
        } catch (Exception e) {
            log.error("{}: error in service(): ", id, e);
        } finally {
            Closeables.close(handler);
            log.debug("{}: end", id);
        }
    }

    private void doUpgrade(String id, HttpServletRequest request, HttpServletResponse response) throws Exception {
        // currently we not support tcp upgrade (but websockets doing in WsProxy)
        // we can send error for docker clients (it accept only 400, 404 & 500 codes),
        // but sending 500 does not interrupt docker client, and its hangs
        request.upgrade(ImmediateCloseConnection.class);
        log.debug("{}: close upgraded connection", id);
    }

    private DefaultFullHttpRequest buildRequest(String id, HttpServletRequest request, String uri) throws IOException {
        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        DefaultFullHttpRequest br = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, uri);
        HttpHeaders bh = br.headers();
        ProxyUtils.copyHeaders(request, bh);
        String transferEncoding = request.getHeader(TRANSFER_ENCODING);
        if("chunked".equals(transferEncoding)) {
            throw new IOException("Unsupported request transfer-encoding: " + transferEncoding);
        }
        int len = request.getContentLength();
        if(len > 0) {
            if(len > MAX_FRONT_REQ_LEN) {
                throw new IOException("Request content too long: " + len + " max allowed: " + MAX_FRONT_REQ_LEN);
            }
            try(ServletInputStream is = request.getInputStream()) {
                br.content().writeBytes(is, len);
            }
        }
        return br;
    }
}
