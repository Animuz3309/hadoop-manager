package edu.scut.cs.hm.model.registry;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;

public class RegistryAuthInterceptor implements ClientHttpRequestInterceptor {

    private final RegistryAuthAdapter adapter;

    public RegistryAuthInterceptor(RegistryAuthAdapter adapter) {
        this.adapter = adapter;
        Assert.notNull(adapter, "adapter can't be null");
    }

    @Override
    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body,
                                        final ClientHttpRequestExecution execution) throws IOException {
        final HttpHeaders headers = request.getHeaders();
        ClientHttpResponse execute = execution.execute(request, body);

        if (execute.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            List<String> list = execute.getHeaders().get("Www-Authenticate");
            if (!CollectionUtils.isEmpty(list)) {
                String tokenString = list.get(0);
                RegistryAuthAdapter.AuthContext ctx = new RegistryAuthAdapter.AuthContext(headers,
                        HttpHeaders.readOnlyHttpHeaders(headers),
                        tokenString);
                adapter.handle(ctx);
                return execution.execute(request, body);
            }
        }
        return execute;
    }

}
