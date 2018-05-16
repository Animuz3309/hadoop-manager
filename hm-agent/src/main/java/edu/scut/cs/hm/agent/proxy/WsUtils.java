package edu.scut.cs.hm.agent.proxy;

import io.netty.handler.codec.http.websocketx.WebSocketVersion;

import javax.servlet.http.HttpServletRequest;

final class WsUtils {
    private WsUtils() {
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
}
