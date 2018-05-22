package edu.scut.cs.hm.agent.proxy;

import com.google.common.collect.Iterators;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

final class ProxyUtils {
    private ProxyUtils() {
    }

    static String reconstructUri(HttpServletRequest request) {
        String q = request.getQueryString();
        String req = request.getRequestURI();
        if(q == null) {
            return req;
        }
        return req + "?" + q;
    }

    static WebSocketVersion getWsVersion(String str) {
        switch (str) {
            case "0":
                return WebSocketVersion.V00;
            case "7":
                return WebSocketVersion.V07;
            case "8":
                return WebSocketVersion.V08;
            case "13":
                return WebSocketVersion.V13;
        }
        return WebSocketVersion.UNKNOWN;
    }

    static void copyHeaders(HttpServletRequest request, HttpHeaders to) {
        Enumeration<String> headers = request.getHeaderNames();
        while(headers.hasMoreElements()) {
            String header = headers.nextElement();
            Iterable<String> iter = () -> Iterators.forEnumeration(request.getHeaders(header));
            to.add(header, iter);
        }
    }
}
