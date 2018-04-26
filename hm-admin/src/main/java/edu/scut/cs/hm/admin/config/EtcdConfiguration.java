package edu.scut.cs.hm.admin.config;

import edu.scut.cs.hm.admin.config.configurer.KvConfigurer;
import edu.scut.cs.hm.kv.etcd.EtcdClientWrapper;
import lombok.extern.slf4j.Slf4j;
import mousio.etcd4j.EtcdClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@EnableConfigurationProperties(KvConfigurer.class)
public class EtcdConfiguration {

    @Bean
    public EtcdClientWrapper client(KvConfigurer config) {
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
}
