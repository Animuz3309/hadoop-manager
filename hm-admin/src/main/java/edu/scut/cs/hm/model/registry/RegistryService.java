package edu.scut.cs.hm.model.registry;

import edu.scut.cs.hm.docker.model.image.ImageDescriptor;
import edu.scut.cs.hm.model.SupportSearch;
import edu.scut.cs.hm.docker.model.image.ImageCatalog;
import edu.scut.cs.hm.docker.model.image.SearchResult;
import edu.scut.cs.hm.docker.model.image.Tags;
import org.springframework.cache.annotation.Cacheable;

/**
 * Docker registry read-only API
 */
public interface RegistryService extends SupportSearch {

    /**
     * Retrieve a sorted, json list of repositories available in the registry.
     * @return ImageCatalog
     */
    @Cacheable("ImageCatalog")
    ImageCatalog getCatalog();

    /**
     * Fetch the tags under the repository identified by name.
     * @param name
     * @return tags with sorted list or null when image not found
     */
    @Cacheable("Tags")
    Tags getTags(String name);

    /**
     * Create Image description from manifest
     * @param name Name of the target repository.
     * @param reference Tag or digest of the target manifest.
     * @return Image or null when image not found
     */
    @Cacheable("ImageDescriptor")
    ImageDescriptor getImage(String name, String reference);

    /**
     * simplified method
     * @param fullImageName
     * @return
     */
    @Cacheable("ImageDescriptor")
    ImageDescriptor getImage(String fullImageName);
    /**
     * Delete the manifest identified by name and reference where reference can be a tag or digest.
     * @param name
     * @param reference
     */
    void deleteTag(String name, String reference);

    /**
     * Registry service configuration
     * @return
     */
    RegistryConfig getConfig();

    /**
     * check health of registry
     * @return
     */
    boolean checkHealth();

    @Override
    @Cacheable("SearchResult")
    SearchResult search(String searchTerm, int page, int count);

    /**
     * Remove registry prefix from image name, if it exists.
     * @param name with or without prefix
     * @return image name without prefix
     */
    String toRelative(String name);

    /**
     * Credentials for login into registry for docker service.
     * @return
     */
    RegistryCredentials getCredentials();
}