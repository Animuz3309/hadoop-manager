package edu.scut.cs.hm.model.registry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.*;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.util.StringUtils;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;

import java.io.IOException;
import java.net.URI;

import static java.text.MessageFormat.format;

/**
 * Interceptor to {@link AsyncRestTemplate} to add docker registry auth
 */
@SuppressWarnings("deprecation")
@RequiredArgsConstructor
@Slf4j
public class HttpAuthInterceptor implements ClientHttpRequestInterceptor, AsyncClientHttpRequestInterceptor {

    private static final ThreadLocal<String> registryName = new ThreadLocal<>();

    private final RegistryRepository registryRepository;

    @Override
    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body,
                                        final ClientHttpRequestExecution execution) throws IOException {
        try {
            final HttpHeaders headers = request.getHeaders();
            interceptInner(headers, request);
            return execution.execute(request, body);
        } finally {
            registryName.remove();
        }
    }

    private void interceptInner(HttpHeaders headers, HttpRequest httpRequest) {
        URI uri = httpRequest.getURI();
        String host = uri.getHost();
        int port = uri.getPort();
        String url = host + (port == -1 ? "" : ":" + port);
        String name = registryName.get();
        log.debug("try to auth request to registry: {}", name);
        RegistryService registry = registryRepository.getByName(name);
        if (registry == null) {
            log.debug("auth : none due to unknown registry \"{}\"", name);
            return;
        }
        RegistryCredentials credentials = registry.getCredentials();
        if (credentials == null || !StringUtils.hasText(credentials.getPassword())) {
            log.debug("auth : none due to unknown registry \"{}\"", name);
            return;
        }
        String result = format("'{'\"username\":\"{0}\",\"password\":\"{1}\",\"email\":\"test@test.com\",\"serveraddress\":\"{2}\",\"auth\":\"\"'}'",
                credentials.getUsername(), credentials.getPassword(), url);
        log.debug("auth : {}", result);
        String xRegistryAuth = new String(Base64.encode(result.getBytes()));
        log.debug("X-Registry-Auth : [{}]", xRegistryAuth);
        headers.add("X-Registry-Auth", xRegistryAuth);
    }

    /**
     * Set registry name
     * @param name
     */
    public static void setCurrentName(String name) {
        registryName.set(name);
    }

    @SuppressWarnings("deprecation")
    @Override
    public ListenableFuture<ClientHttpResponse> intercept(HttpRequest request,
                                                          byte[] body,
                                                          AsyncClientHttpRequestExecution execution) throws IOException {
        try {
            final HttpHeaders headers = request.getHeaders();
            interceptInner(headers, request);
            return execution.executeAsync(request, body);
        } finally {
            registryName.remove();
        }
    }
}
