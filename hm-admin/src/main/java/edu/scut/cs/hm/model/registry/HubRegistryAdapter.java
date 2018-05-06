package edu.scut.cs.hm.model.registry;

import edu.scut.cs.hm.model.registry.core.RestTemplateFactory;

public class HubRegistryAdapter extends DockerRegistryAdapter<HubRegistryConfig> {

    private final String url;

    public HubRegistryAdapter(HubRegistryConfig config, RestTemplateFactory rtf, String url) {
        super(config, rtf);
        this.url = url;
    }

    @Override
    public String adaptNameForUrl(String name) {
        if(name.indexOf('/') < 0) {
            // it need because docker hub find simple names like 'nginx' under 'library/nginx' path
            return "library/" + name;
        }
        return name;
    }

    @Override
    public String getUrl() {
        return url;
    }
}
