package edu.scut.cs.hm.agent.proxy;

import io.netty.handler.codec.http.websocketx.WebSocketVersion;

final class WsUtils {
    private WsUtils() {

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
