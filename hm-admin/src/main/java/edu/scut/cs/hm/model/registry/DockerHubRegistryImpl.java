package edu.scut.cs.hm.model.registry;

import edu.scut.cs.hm.docker.model.image.ImageCatalog;
import edu.scut.cs.hm.docker.model.image.SearchResult;
import edu.scut.cs.hm.model.registry.core.DockerHubRegistry;
import edu.scut.cs.hm.model.registry.core.RegistryAdapter;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

@Slf4j
public class DockerHubRegistryImpl extends AbstractV2RegistryService implements DockerHubRegistry {

    @Builder
    public DockerHubRegistryImpl(RegistryAdapter adapter) {
        super(adapter);
    }

    @Override
    public SearchResult search(String searchTerm, int page, int count) {
        return new SearchResult();
    }

    @Override
    public ImageCatalog getCatalog() {
        return new ImageCatalog(Collections.emptyList());
    }

    @Override
    public String toRelative(String name) {
        return name;
    }
}
