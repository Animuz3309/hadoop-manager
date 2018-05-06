package edu.scut.cs.hm.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import edu.scut.cs.hm.admin.config.configurer.RegistriesConfigurer;
import edu.scut.cs.hm.common.utils.HttpUserAgentInterceptor;
import edu.scut.cs.hm.model.registry.*;
import edu.scut.cs.hm.model.registry.core.DockerHubRegistry;
import edu.scut.cs.hm.model.registry.core.RegistryAuthAdapter;
import edu.scut.cs.hm.model.registry.core.RegistryFactoryAdapter;
import edu.scut.cs.hm.model.registry.core.RegistryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
@Service
public class RegistryFactory implements DisposableBean {
    private final ScheduledExecutorService scheduledExecutorService;
    private final Map<Class<?>, RegistryFactoryAdapter> adapters;

    private ObjectMapper objectMapper;
    private Integer connectTimeOut = 10000;
    private Integer readTimeOut = 20000;
    private String dockerSearchHubUrl = "https://registry.hub.docker.com";
    private String dockerHubUrl = "https://registry-1.docker.io";
    private long searchCacheMinutes = 10;

    @Autowired
    public RegistryFactory(ObjectMapper objectMapper, RegistriesConfigurer configurer) {
        this.objectMapper = objectMapper;
        if (configurer != null) {
            connectTimeOut = configurer.getConnectTimeOut();
            readTimeOut = configurer.getReadTimeOut();
            dockerSearchHubUrl = configurer.getDockerSearchHubUrl();
            dockerHubUrl = configurer.getDockerHubUrl();
            searchCacheMinutes = configurer.getSearchCacheMinutes();
        }

        this.adapters = ImmutableMap.of(
                HubRegistryConfig.class, new RegistryFactoryAdapter<HubRegistryConfig>() {
                    @Override
                    public RegistryService create(RegistryFactory factory, HubRegistryConfig config) {
                        return createHubRegistryService(config);
                    }

                    @Override
                    public void complete(HubRegistryConfig config) {
                        if(config.getName() != null) {
                            return;
                        }
                        config.setName(config.getUsername());
                    }
                },
                PrivateRegistryConfig.class, new RegistryFactoryAdapter<PrivateRegistryConfig>() {
                    @Override
                    public RegistryService create(RegistryFactory factory, PrivateRegistryConfig config) {
                        return RegistryServiceImpl.builder()
                                .adapter(new PrivateRegistryAdapter(config, RegistryFactory.this::restTemplate))
                                .searchConfig(getSearchIndexDefaultConfig())
                                .build();
                    }

                    @Override
                    public void complete(PrivateRegistryConfig config) {
                        if(config.getName() != null) {
                            return;
                        }
                        String name = RegistryUtils.getNameByUrl(config.getUrl());
                        config.setName(name);
                    }
                }
        );

        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat(getClass().getSimpleName() + "-scheduled-%d")
                .build());
    }

    public RestTemplate restTemplate(RegistryAuthAdapter registryAuthAdapter) {
        RestTemplate restTemplate = new RestTemplate();
        List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();
        SimpleClientHttpRequestFactory rf =
                (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
        rf.setReadTimeout(readTimeOut);
        rf.setConnectTimeout(connectTimeOut);

        restTemplate.setInterceptors(ImmutableList.of(
                new RegistryAuthInterceptor(registryAuthAdapter),
                HttpUserAgentInterceptor.getDefault()
        ));

        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                MappingJackson2HttpMessageConverter jsonConverter = (MappingJackson2HttpMessageConverter) converter;
                jsonConverter.setObjectMapper(objectMapper);
                //note that this content-type will be placed in Acceptable header
                jsonConverter.setSupportedMediaTypes(Arrays.asList(
                        new MediaType("application", "json", MappingJackson2HttpMessageConverter.DEFAULT_CHARSET),
                        new MediaType("application", "*+json", MappingJackson2HttpMessageConverter.DEFAULT_CHARSET),
                        // it need for blobs (with json) from registry
                        new MediaType("application", "octet-stream", MappingJackson2HttpMessageConverter.DEFAULT_CHARSET)
                ));
            }
        }
        return restTemplate;
    }

    public DockerHubRegistry createHubRegistryService(HubRegistryConfig config) {
        DockerHubRegistryImpl registryService = DockerHubRegistryImpl.builder()
                .adapter(new HubRegistryAdapter(config, this::restTemplate, dockerHubUrl))
                .build();
        return new DockerHubRegistryServiceWrapper(registryService, config.getUsername());
    }

    public DockerHubRegistry createPublicHubRegistryService(HubRegistryConfig config) {
        PublicDockerHubRegistryImpl registryService = PublicDockerHubRegistryImpl.builder()
                .adapter(new HubRegistryAdapter(config, this::restTemplate, dockerHubUrl))
                .dockerHubSearchRegistryUrl(dockerSearchHubUrl)
                .build();
        return registryService;

    }

    public SearchIndex.Config getSearchIndexDefaultConfig() {
        SearchIndex.Config config = new SearchIndex.Config();
        config.setScheduledExecutorService(scheduledExecutorService);
        config.setCacheMinutes(this.searchCacheMinutes);
        return config;
    }

    public <T extends RegistryConfig> RegistryService createRegistryService(T config) {
        complete(config);
        RegistryFactoryAdapter<T> adapter = getTypeAdapter(config);
        return adapter.create(this, config);
    }

    public <T extends RegistryConfig> void complete(T config) {
        RegistryFactoryAdapter<T> adapter = getTypeAdapter(config);
        adapter.complete(config);
    }

    @SuppressWarnings("unchecked")
    private <T extends RegistryConfig> RegistryFactoryAdapter<T> getTypeAdapter(T config) {
        Class<?> type = config.getClass();
        RegistryFactoryAdapter<RegistryConfig> adapter = adapters.get(type);
        Assert.notNull(adapter, "can not find adapter for " + type);
        return (RegistryFactoryAdapter<T>) adapter;
    }

    /**
     * Invoked by a BeanFactory on destruction of a singleton.
     *
     * @throws Exception in case of shutdown errors.
     *                   Exceptions will get logged but not rethrown to allow
     *                   other beans to release their resources too.
     */
    @Override
    public void destroy() throws Exception {
        this.scheduledExecutorService.shutdownNow();
    }
}
