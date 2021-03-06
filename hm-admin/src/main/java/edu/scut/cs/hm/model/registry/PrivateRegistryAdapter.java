package edu.scut.cs.hm.model.registry;

import edu.scut.cs.hm.model.registry.core.RestTemplateFactory;

public class PrivateRegistryAdapter extends DockerRegistryAdapter<PrivateRegistryConfig> {

    public PrivateRegistryAdapter(PrivateRegistryConfig config, RestTemplateFactory rtf) {
        super(config, rtf);
    }

    @Override
    public String getUrl() {
        return config.getUrl();
    }
}
