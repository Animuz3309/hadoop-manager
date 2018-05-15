package edu.scut.cs.hm.common.utils;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class HttpUserAgentInterceptor implements ClientHttpRequestInterceptor {

    private static final HttpUserAgentInterceptor DEFAULT = new HttpUserAgentInterceptor("Haven");

    private final String userAgent;

    public HttpUserAgentInterceptor(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        request.getHeaders().set("User-Agent", "Haven");
        return execution.execute(request, body);
    }

    /**
     * Interceptor which install default user-agent.
     * @return interceptor
     */
    public static HttpUserAgentInterceptor getDefault() {
        return DEFAULT;
    }
}