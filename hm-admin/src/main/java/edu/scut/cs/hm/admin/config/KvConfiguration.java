package edu.scut.cs.hm.admin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.scut.cs.hm.admin.config.configurer.KvConfigurer;
import edu.scut.cs.hm.common.kv.KeyValueStorage;
import edu.scut.cs.hm.common.kv.etcd.EtcdClientWrapper;
import edu.scut.cs.hm.common.kv.mapping.KvMapperFactory;
import lombok.extern.slf4j.Slf4j;
import mousio.etcd4j.EtcdClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import javax.validation.Validator;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@EnableConfigurationProperties(KvConfigurer.class)
public class KvConfiguration {

    @Bean
    public KeyValueStorage keyValueStorage(KvConfigurer config) {
        List<URI> uris = new ArrayList<>();
        String[] etcdUrls = config.getEtcdUrls();
        if (etcdUrls != null) {
            for (String etcdUrl: etcdUrls) {
                uris.add(URI.create(etcdUrl));
            }
        }
        log.info("About to connect to etcd: {}", (Object)etcdUrls);
        EtcdClient etcd = new EtcdClient(uris.toArray(new URI[0]));
        return new EtcdClientWrapper(etcd, config.getPrefix().trim());
    }

    @Bean
    public KvMapperFactory kvMapperFactory(ObjectMapper objectMapper, KeyValueStorage storage, TextEncryptor encryptor, Validator validator) {
        return new KvMapperFactory(objectMapper, storage, encryptor, validator);
    }
}
