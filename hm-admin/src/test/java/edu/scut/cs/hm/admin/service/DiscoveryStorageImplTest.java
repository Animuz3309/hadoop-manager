package edu.scut.cs.hm.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.scut.cs.hm.admin.component.ContainerCreator;
import edu.scut.cs.hm.admin.component.FilterFactory;
import edu.scut.cs.hm.admin.config.*;
import edu.scut.cs.hm.admin.security.AccessContext;
import edu.scut.cs.hm.admin.security.AccessContextFactory;
import edu.scut.cs.hm.common.kv.mapping.KvMapperFactory;
import edu.scut.cs.hm.common.mb.MessageBus;
import edu.scut.cs.hm.docker.*;
import edu.scut.cs.hm.model.cluster.DiscoveryStorage;
import edu.scut.cs.hm.model.container.ContainerStorage;
import edu.scut.cs.hm.model.ngroup.DockerClusterConfig;
import edu.scut.cs.hm.model.ngroup.NodesGroup;
import edu.scut.cs.hm.model.ngroup.NodesGroupEvent;
import edu.scut.cs.hm.model.node.NodeEvent;
import edu.scut.cs.hm.model.node.NodeInfoProvider;
import edu.scut.cs.hm.model.registry.HttpAuthInterceptor;
import edu.scut.cs.hm.model.registry.RegistryRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.AsyncRestTemplate;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings({"deprecation", "unchecked"})
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(classes = DiscoveryStorageImplTest.Config.class)
@TestPropertySource(properties = {
        "host            = localhost:2381",
        "hm.kv.etcd-urls = http://localhost:2370",
        "hm.kv.prefix    = /test"})
public class DiscoveryStorageImplTest {
    private static final String REAL = "real";
    public static final String MOCK = "mock";

    @Configuration
    @Import({KvConfiguration.class,
            JacksonConfiguration.class,
            MbConfiguration.class,
            DockerConfiguration.class})
    public static class Config {

        @Value("${host}")
        private String host;

        @Bean
        DiscoveryStorage discoveryStorage(KvMapperFactory kvmf,
                                          FilterFactory filter,
                                          DockerServices dockerServices,
                                          NodeStorage nodeStorage,
                                          AccessContextFactory acl,
                                          AutowireCapableBeanFactory beanFactory,
                                          @Qualifier(NodesGroupEvent.BUS) MessageBus<NodesGroupEvent> messageBus) {
            return new DiscoveryStorageImpl(
                    kvmf,
                    filter,
                    dockerServices,
                    nodeStorage,
                    acl,
                    beanFactory,
                    messageBus);
        }

        @Bean
        ContainerCreator containerCreator() {
            ContainerCreator cc = mock(ContainerCreator.class);
            return cc;
        }

        @Bean
        ContainerStorage containerStorage() {
            ContainerStorage cs = mock(ContainerStorage.class);
            return cs;
        }

        @Bean
        AccessContextFactory accessContextFactory() {
            AccessContextFactory acf = mock(AccessContextFactory.class);
            AccessContext ac = mock(AccessContext.class);
            when(ac.isGranted(any(), any())).thenReturn(true);
            when(acf.getContext()).thenReturn(ac);
            return acf;
        }

        @Bean
        FilterFactory filterFactory() {
            return new FilterFactory();
        }

        @Bean
        TextEncryptor textEncryptor() {
            return Encryptors.noOpText();
        }

        @Bean
        NodeStorage nodeStorage(@Qualifier(NodeEvent.BUS) MessageBus<NodeEvent> mb) {
            NodeStorage ns = mock(NodeStorage.class);
            when(ns.getNodeEventSubscriptions()).thenReturn(mb);
            return ns;
        }

        @Bean
        DockerServices dockerServices() {
            DockerService mock = new DockerServiceMock(DockerServiceInfo.builder().build());
            DockerService real = dockerService();
            DockerServices dses = mock(DockerServices.class);
            when(dses.getService(MOCK)).thenReturn(mock);
            when(dses.getService(REAL)).thenReturn(real);
            when(dses.getOrCreateDocker(anyObject(), anyObject())).thenReturn(mock);
            return dses;
        }

        DockerServiceImpl dockerService() {
            DockerConfig config = DockerConfig.builder().host(host).build();
            AsyncRestTemplate restTemplate = new AsyncRestTemplate();
            restTemplate.setInterceptors(
                    Collections.singletonList(new HttpAuthInterceptor(mock(RegistryRepository.class)))
            );
            return DockerServiceImpl.builder()
                    .config(config)
                    .cluster("test")
                    .restTemplate(restTemplate)
                    .nodeInfoProvider(mock(NodeInfoProvider.class))
                    .eventConsumer(mock(MessageBus.class))
                    .objectMapper(new ObjectMapper())
                    .build();
        }
    }

    @Autowired
    private DiscoveryStorage storage;

    @Test
    public void test() {
        NodesGroup ng = storage.getOrCreateCluster(MOCK, ccc -> {
            DockerClusterConfig config = new DockerClusterConfig();
            config.setName(ccc.getCluster());
            return config;
        });
        assertNotNull(ng);
        assertEquals(MOCK,ng.getName());
    }
}