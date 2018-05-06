package edu.scut.cs.hm.model.registry;

public class PrivateRegistryAdapter extends DockerRegistryAdapter<PrivateRegistryConfig> {

    public PrivateRegistryAdapter(PrivateRegistryConfig config, RestTemplateFactory rtf) {
        super(config, rtf);
    }

    @Override
    public String getUrl() {
        return config.getUrl();
    }
}
