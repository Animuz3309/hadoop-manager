package edu.scut.cs.hm.admin.bootstrap;

import edu.scut.cs.hm.admin.security.TempAuth;
import edu.scut.cs.hm.admin.service.DiscoveryStorageImpl;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

@Configuration
public class ApplicationBootstrap implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private ObjectFactory<DiscoveryStorageImpl> storageFactory;
    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try (TempAuth auth = TempAuth.asSystem()) {
            DiscoveryStorageImpl storage = storageFactory.getObject();
            storage.load();
        }
    }
}
