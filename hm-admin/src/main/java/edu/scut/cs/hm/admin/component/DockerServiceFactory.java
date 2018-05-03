package edu.scut.cs.hm.admin.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import edu.scut.cs.hm.admin.config.configurer.DockerConfigurer;
import edu.scut.cs.hm.admin.security.AccessContextFactory;
import edu.scut.cs.hm.admin.security.TempAuth;
import edu.scut.cs.hm.admin.service.NodeService;
import edu.scut.cs.hm.common.http.async.NettyRequestFactory;
import edu.scut.cs.hm.common.http.interceptor.BasicAuthAsyncInterceptor;
import edu.scut.cs.hm.common.mb.MessageBus;
import edu.scut.cs.hm.common.utils.AddressUtils;
import edu.scut.cs.hm.common.utils.SSLUtils;
import edu.scut.cs.hm.docker.DockerConfig;
import edu.scut.cs.hm.docker.DockerService;
import edu.scut.cs.hm.docker.DockerServiceImpl;
import edu.scut.cs.hm.docker.DockerServiceSecurityWrapper;
import edu.scut.cs.hm.docker.model.events.DockerServiceEvent;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.JdkSslContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.client.AsyncClientHttpRequestInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.AsyncRestTemplate;

import javax.annotation.PreDestroy;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Factory for Docker service
 */
@Slf4j
@Component
public class DockerServiceFactory {
    // TODO no registryRepository
    private final ObjectMapper objectMapper;
    private final AccessContextFactory aclContextFactory;
    private final MessageBus<DockerServiceEvent> dockerServiceEventMessageBus;
    private final ResourceLoader resourceLoader;
    private final ExecutorService executor;

    private boolean checkSsl;
    /**
     * rootCert.keystore to 'hm-agent' on the other physical docker node
     */
    private String keystore;
    /**
     * rootCert.storepass to 'hm-agent' on the other physical docker node
     */
    private String storepass;
    /**
     * password to 'hm-agent' on the other physical docker node
     */
    private String password;

    @Autowired
    public DockerServiceFactory(ObjectMapper objectMapper,
                                AccessContextFactory aclContextFactory,
                                @Qualifier(DockerServiceEvent.BUS) MessageBus<DockerServiceEvent> dockerServiceEventMessageBus,
                                ResourceLoader resourceLoader,
                                DockerConfigurer.AgentConfig agentConfig) {
        this.objectMapper = objectMapper;
        this.aclContextFactory = aclContextFactory;
        this.dockerServiceEventMessageBus = dockerServiceEventMessageBus;
        this.resourceLoader = resourceLoader;
        this.executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat(getClass().getSimpleName() + "-executor-%d")
                .setUncaughtExceptionHandler((thread, ex) -> log.error("Uncaught exception.", ex))
                .build());

        this.checkSsl = agentConfig.isCheckSsl();
        this.keystore = agentConfig.getRootCertKeystore();
        this.storepass = agentConfig.getRootCertStorepass();
        this.password = agentConfig.getPassword();
    }

    /**
     * Create a Security Wrapped DockerService
     * @param dockerConfig
     * @param dockerConsumer
     * @return
     */
    public DockerService createDockerService(DockerConfig dockerConfig,
                                             NodeService nodeService,
                                             Consumer<DockerServiceImpl.Builder> dockerConsumer) {
        DockerServiceImpl.Builder b = DockerServiceImpl.builder();
        b.config(dockerConfig);

        String cluster = dockerConfig.getCluster();
        if (cluster != null) {
            b.cluster(cluster);
        }

        String address = dockerConfig.getHost();
        b.restTemplate(createRestTemplate(address));
        b.nodeInfoProvider(nodeService);
        b.eventConsumer(this::dockerEventConsumer);

        if (dockerConsumer != null) {
            dockerConsumer.accept(b);
        }
        b.objectMapper(objectMapper);

        DockerService ds = b.build();
        ds = securityWrapper(ds);
        return ds;
    }

    private DockerService securityWrapper(DockerService dockerService) {
        return new DockerServiceSecurityWrapper(aclContextFactory, dockerService);
    }

    private void dockerEventConsumer(DockerServiceEvent dockerServiceEvent) {
        executor.execute(() -> {
            try(TempAuth auth = TempAuth.asSystem()) {
                dockerServiceEventMessageBus.accept(dockerServiceEvent);
            }
        });
    }

    @SuppressWarnings("deprecation")
    private AsyncRestTemplate createRestTemplate(String addr) {
        // we use async client because usual client does not allow to interruption in some case
        NettyRequestFactory factory = new NettyRequestFactory();

        if (AddressUtils.isHttps(addr)) {
            try {
                initSsl(addr, factory);
            } catch (Exception e) {
                log.error("", e);
            }
        }

        final AsyncRestTemplate restTemplate = new AsyncRestTemplate(factory);
        List<AsyncClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        // TODO no registryRepository HttpAuthInterceptor
        if(!StringUtils.isEmpty(password)) {
            interceptors.add(new BasicAuthAsyncInterceptor("admin", password));
        }
        restTemplate.setInterceptors(interceptors);
        return restTemplate;
    }

    private void initSsl(String addr, NettyRequestFactory factory) throws Exception {
        SSLContext sslc = SSLContext.getInstance("TLS");
        if (!checkSsl) {
            log.debug("disable any SSL check on {} address", addr);
            sslc.init(null, new TrustManager[]{new SSLUtils.NullX509TrustManager()}, null);
        } else if(StringUtils.hasText(keystore)) {
            log.debug("use SSL trusted store {} on {} address", keystore, addr);
            final String alg = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory def = TrustManagerFactory.getInstance(alg);
            def.init((KeyStore)null);// initialize default list of trust managers
            Resource resource = resourceLoader.getResource(keystore);
            if(!resource.exists()) {
                log.warn("Specified JKS {} is not exists.", keystore);
                return;
            }
            KeyStore ks = KeyStore.getInstance("JKS");
            try(InputStream is = resource.getInputStream()) {
                ks.load(is, storepass == null? new char[0] : storepass.toCharArray());
            }
            TrustManagerFactory local = TrustManagerFactory.getInstance(alg);
            local.init(ks);
            TrustManager tm = SSLUtils.combineX509TrustManagers(local.getTrustManagers(), def.getTrustManagers());
            sslc.init(null, new TrustManager[]{tm}, null);
        }
        factory.setSslContext(new JdkSslContext(sslc, true, ClientAuth.OPTIONAL));
    }

    @PreDestroy
    private void preDestroy() {
        this.executor.shutdownNow();
    }
}
