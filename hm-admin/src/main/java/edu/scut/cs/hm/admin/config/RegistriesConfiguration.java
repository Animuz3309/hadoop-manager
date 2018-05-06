package edu.scut.cs.hm.admin.config;

import com.google.common.base.MoreObjects;
import edu.scut.cs.hm.admin.config.configurer.RegistriesConfigurer;
import edu.scut.cs.hm.admin.service.RegistryFactory;
import edu.scut.cs.hm.common.kv.mapping.KvMapperFactory;
import edu.scut.cs.hm.common.mb.MessageBus;
import edu.scut.cs.hm.common.mb.MessageBuses;
import edu.scut.cs.hm.common.utils.SSLUtil;
import edu.scut.cs.hm.model.registry.*;
import edu.scut.cs.hm.model.registry.core.DockerHubRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@EnableConfigurationProperties(RegistriesConfigurer.class)
@Configuration
public class RegistriesConfiguration {
    @Value("${dm.ssl.check:true}")
    private Boolean checkSsl;

    private RegistryFactory registryFactory;


    @Autowired
    public void setRegistryFactory(RegistryFactory registryFactory) {
        this.registryFactory = registryFactory;
    }

    @Bean
    @Lazy
    RegistryRepository registryService(KvMapperFactory factory,
                                       RegistriesConfigurer configurer,
                                       @Qualifier(RegistryEvent.BUS) MessageBus<RegistryEvent> messageBus) {
        if (!checkSsl) {
            SSLUtil.disable();
        }

        HubRegistryConfig defaultRegistryConf = new HubRegistryConfig();
        defaultRegistryConf.setName(DockerHubRegistry.DEFAULT_NAME);
        defaultRegistryConf.setTitle("Docker Hub");
        defaultRegistryConf.setEditable(false);
        defaultRegistryConf.setReadOnly(true);
        DockerHubRegistry defaultRegistry = registryFactory.createPublicHubRegistryService(defaultRegistryConf);
        RegistryRepository registryRepository = new RegistryRepository(factory, defaultRegistry, registryFactory, messageBus);

        List<RegistryConfig> args = new ArrayList<>();
        args.addAll(MoreObjects.firstNonNull(configurer.getPrivateRegistry(), Collections.emptyList()));
        args.addAll(MoreObjects.firstNonNull(configurer.getHubRegistry(), Collections.emptyList()));

        if(configurer.isSyncInit()) {
            registryRepository.init(args);
        } else {
            new Thread(() -> registryRepository.init(args), "registry-init").start();
        }


        return registryRepository;
    }

    @Bean(name = RegistryEvent.BUS)
    MessageBus<RegistryEvent> registryEventMessageBus() {
        return MessageBuses.create(RegistryEvent.BUS, RegistryEvent.class);
    }
}
