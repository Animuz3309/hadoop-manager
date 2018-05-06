package edu.scut.cs.hm.model.registry;

import edu.scut.cs.hm.docker.model.image.ImageCatalog;
import edu.scut.cs.hm.docker.model.image.ImageDescriptor;
import edu.scut.cs.hm.docker.model.image.SearchResult;
import edu.scut.cs.hm.docker.model.image.Tags;
import edu.scut.cs.hm.model.registry.core.RegistryCredentials;
import edu.scut.cs.hm.model.registry.core.RegistryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

/**
 * Docker registry read-only API
 */
@Slf4j
@RequiredArgsConstructor
public class DisabledRegistryServiceWrapper implements RegistryService {

    private final RegistryService registryService;

    public ImageCatalog getCatalog() {
        logWarn();
        return null;
    }

    private void logWarn() {
        log.error("Registry is disabled {} ", getConfig());
    }

    public Tags getTags(String name) {
        logWarn();
        return null;
    }

    public Manifest getManifest(String name, String reference) {
        logWarn();
        return null;
    }

    public ImageDescriptor getImage(String name, String reference) {
        logWarn();
        return null;
    }

    @Override
    public ImageDescriptor getImage(String fullImageName) {
        logWarn();
        return null;
    }

    @Override
    public void deleteTag(String name, String reference) {
        logWarn();
        throw new IllegalStateException("Registry is disabled");
    }

    @Override
    public RegistryConfig getConfig() {
        return registryService.getConfig();
    }

    @Override
    public RegistryCredentials getCredentials() {
        return registryService.getCredentials();
    }

    @Override
    public boolean checkHealth() {
        logWarn();
        return false;
    }

    @Override
    public String toRelative(String name) {
        return registryService.toRelative(name);
    }

    @Override
    public SearchResult search(String searchTerm, int page, int count) {
        SearchResult result = new SearchResult();
        result.setPage(0);
        result.setNumPages(1);
        result.setResults(Collections.emptyList());
        result.setQuery(searchTerm);
        result.setPageSize(count);
        result.setNumResults(0);
        return result;
    }
}
