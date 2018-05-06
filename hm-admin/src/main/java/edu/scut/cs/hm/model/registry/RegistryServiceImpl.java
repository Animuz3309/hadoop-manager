package edu.scut.cs.hm.model.registry;

import edu.scut.cs.hm.docker.model.image.SearchResult;
import lombok.Builder;
import org.springframework.beans.factory.InitializingBean;

/**
 * REST implementation of RegistryService
 */
public class RegistryServiceImpl extends AbstractV2RegistryService implements AutoCloseable, InitializingBean {

    private final SearchIndex searchIndex;

    @Builder
    public RegistryServiceImpl(RegistryAdapter adapter,
                               SearchIndex.Config searchConfig) {
        super(adapter);
        this.searchIndex = new SearchIndex(this, searchConfig);
    }

    @Override
    public SearchResult search(String searchTerm, int page, int count) {
        return searchIndex.search(searchTerm, page, count);
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        //test of registry
        checkHealth();
        searchIndex.init();
    }

    @Override
    public void close() throws Exception {
        this.searchIndex.close();
    }
}
