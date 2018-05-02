package edu.scut.cs.hm.common.http.interceptor;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.AsyncClientHttpRequestExecution;
import org.springframework.http.client.AsyncClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;
import org.springframework.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Based on {@link org.springframework.http.client.support.BasicAuthorizationInterceptor } which is not support
 * async client.
 */
public class BasicAuthAsyncInterceptor implements AsyncClientHttpRequestInterceptor {

    private final String username;

    private final String password;

    public BasicAuthAsyncInterceptor(String username, String password) {
        Assert.hasLength(username, "Username must not be empty");
        this.username = username;
        this.password = (password != null ? password : "");
    }

    @Override
    public ListenableFuture<ClientHttpResponse> intercept(HttpRequest request, byte[] body, AsyncClientHttpRequestExecution execution) throws IOException {
        String token = Base64Utils.encodeToString((this.username + ":" + this.password).getBytes(StandardCharsets.UTF_8));
        request.getHeaders().add("Authorization", "Basic " + token);
        return execution.executeAsync(request, body);
    }
}