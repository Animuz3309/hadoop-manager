package edu.scut.cs.hm.agent.notifier;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.scut.cs.hm.agent.config.props.NotifierProperties;
import edu.scut.cs.hm.common.utils.AbstractAutostartup;
import edu.scut.cs.hm.common.utils.ExecutorUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PreDestroy;
import java.net.URI;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
class Notifier extends AbstractAutostartup {

    private final ScheduledExecutorService executor;
    private final RestTemplate restTemplate;
    private final URI url;
    private final ObjectMapper objectMapper;
    private final String secret;
    private final DataProvider dataProvider;

    @Autowired
    Notifier(NotifierProperties notifierProperties, RestTemplate restTemplate, ObjectMapper objectMapper, DataProvider dataProvider) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.dataProvider = dataProvider;
        this.url = calculateUrl(notifierProperties);
        this.secret = notifierProperties.getSecret();

        this.executor = ExecutorUtils.singleThreadScheduledExecutor(this.getClass());
        if(url != null) {
            log.warn("Server url is '{}', schedule notifier.", url);
            ScheduledFuture<?> future = this.executor.scheduleWithFixedDelay(this::send,
                    notifierProperties.getInitialDelay(),
                    notifierProperties.getPeriod(),
                    TimeUnit.SECONDS);
            addToClose(() -> future.cancel(true));
        } else {
            log.warn("Server url is null, disable notifier.");
        }

        addToClose(this.executor::shutdownNow);
    }

    private URI calculateUrl(NotifierProperties notifierProperties) {
        String server = notifierProperties.getServer();
        if(!StringUtils.hasText(server)) {
            return null;
        }
        return URI.create(server + "/discovery/nodes/" + dataProvider.getHostname());
    }

    private void send() {
        try {
            NotifierData data = dataProvider.getData();
            HttpHeaders headers = new HttpHeaders();
            if(secret != null) {
                headers.set(NotifierData.HEADER, secret);
            }
            RequestEntity<NotifierData> req = new RequestEntity<>(data, headers, HttpMethod.POST, url);
            ResponseEntity<String> resp = restTemplate.exchange(req, String.class);
            if(log.isDebugEnabled()) {
                log.debug("Send data {} to {}, with result: {}", objectMapper.writeValueAsString(data), url, resp.getStatusCode());
            }
        } catch (Exception e) {
            if(e instanceof ResourceAccessException) {
                // we reduce stack trace of some errors
                log.error("Can not send to {}, due to error: {}", url, e.toString());
            } else {
                log.error("Can not send to {}", url, e);
            }
        }
    }

    @PreDestroy
    public void cleanUp() {
        executor.shutdownNow();
    }
}
