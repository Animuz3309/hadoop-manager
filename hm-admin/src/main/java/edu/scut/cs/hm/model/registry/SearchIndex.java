package edu.scut.cs.hm.model.registry;

import edu.scut.cs.hm.common.utils.SingleValueCache;
import edu.scut.cs.hm.docker.model.image.*;
import edu.scut.cs.hm.model.StandardAction;
import edu.scut.cs.hm.model.SupportSearch;
import edu.scut.cs.hm.model.container.ContainerUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SearchIndex implements SupportSearch, AutoCloseable {


    @Data
    public static class Config {
        private long cacheMinutes = 2L;
        private ScheduledExecutorService scheduledExecutorService;
    }

    public static final String LABEL_DESCRIPTION = "description";
    private final long timeout;
    private final RegistryService service;
    private final SingleValueCache<Map<String, ImageInfo>> cache;
    private final String registryName;
    private final ScheduledExecutorService ses;
    private ScheduledFuture<?> future;

    public SearchIndex(RegistryService service, Config config) {
        this.service = service;
        this.registryName = this.service.getConfig().getName();
        this.ses = config.getScheduledExecutorService();
        this.timeout = TimeUnit.MINUTES.toMillis(config.cacheMinutes);
        this.cache = SingleValueCache.builder(this::load).timeAfterWrite(TimeUnit.MILLISECONDS, getTimeout()).build();
    }

    private Map<String, ImageInfo> load() {
        long begin = System.currentTimeMillis();
        //sometime we may found duplicates
        String regId = registryName + "@" + Objects.hashCode(service);
        log.info("Begin load index of {} ", regId);
        Map<String, ImageInfo> old = this.cache.getOldValue();
        Map<String, ImageInfo> images = new HashMap<>();
        ImageCatalog catalog = this.service.getCatalog();
        if(catalog == null) {
            log.info("Catalog of {} is null, see above log for details.", regId);
        } else {
            for(String image: catalog.getImages()) {
                String fullName = ContainerUtils.buildImageName(registryName, image, null);
                ImageDescriptor descriptor = loadDescriptor(regId, image);
                ImageInfo ii = new ImageInfo(fullName, descriptor);
                images.put(fullName, ii);
            }
        }
        float seconds = (System.currentTimeMillis() - begin)/1000f;
        log.info("End load index of {} in {} seconds, loaded {} records", regId, seconds, images.size());
        if(service instanceof AbstractV2RegistryService && !Objects.equals(old, images)) {
            // we detect difference in image catalogs and send update event
            ((AbstractV2RegistryService)service).fireEvent(RegistryEvent.builder().action(StandardAction.UPDATE.value()));
        }
        return images;
    }

    private ImageDescriptor loadDescriptor(String regId, String image) {
        // we use descriptor of latest image
        ImageDescriptor descriptor = null;
        String latestTag = "latest";
        try {
            descriptor = this.service.getImage(image, latestTag);
            if(descriptor == null) {
                //not any image has 'latest' tag and we may try load tags
                Tags tags = this.service.getTags(image);
                if(tags == null) {
                    log.info("Tags of image {} from registry {} is null, see above log for details.", image, regId);
                } else {
                    List<String> list = tags.getTags();
                    if(!CollectionUtils.isEmpty(list)) {
                        //order of tags is sometime random and we need to sort them
                        list.sort(ImageNameComparator.getTagsComparator());
                        latestTag = list.get(list.size() - 1);
                        descriptor = this.service.getImage(image, latestTag);
                    }
                }
            }
        } catch (Exception e) {
            // for prevent noise in log (it may happen when registry is down) we do not print stack trace
            log.info("Can not load latest image {} from registry {} with error: {}", image, regId, e.toString());
        }
        return descriptor;
    }

    @Override
    public SearchResult search(String query, int page, int count) {
        Assert.hasText(query, "query is null");
        SearchResult result = new SearchResult();
        result.setPage(0);
        result.setNumPages(1);
        result.setQuery(query);
        List<SearchResult.Result> results = new ArrayList<>();
        result.setResults(results);
        Map<String, ImageInfo> images = getImages();
        for(String fullImageName: images.keySet()) {
            boolean match = fullImageName == null ? query == null : query != null && fullImageName.contains(query);
            if(match) {
                SearchResult.Result res = new SearchResult.Result();
                res.setName(fullImageName);
                ImageInfo ii = images.get(fullImageName);
                String description = getDescription(ii);
                res.setDescription(description);
                res.getRegistries().add(registryName);
                results.add(res);
            }
        }
        results.sort(null);
        result.setNumResults(results.size());
        result.setPageSize(result.getNumResults());
        return result;
    }

    private Map<String, ImageInfo> getImages() {
        Map<String, ImageInfo> images = cache.getOrNull();
        if(images == null) {
            images = cache.getOldValue();
        }
        if(images == null) {
            images = cache.get();
        }
        return images;
    }

    private String getDescription(ImageInfo ii) {
        String description = null;
        ImageDescriptor descriptor = ii.getDescriptor();
        if(descriptor != null) {
            Map<String, String> labels = descriptor.getLabels();
            description = labels == null? null :  labels.get(LABEL_DESCRIPTION);
        }
        if(description == null) {
            description = "";
        }
        return description;
    }

    public void init() {
        if(ses != null) {
            //TODO we must return old cache when update in progress
            this.future = ses.scheduleWithFixedDelay(cache::get, 1000L, getTimeout(), TimeUnit.MILLISECONDS);
        }
    }

    private long getTimeout() {
        return timeout;
    }

    @Override
    public void close() throws Exception {
        ScheduledFuture<?> future = this.future;
        if(future != null) {
            future.cancel(true);
        }
    }

    @EqualsAndHashCode
    public static class ImageInfo {
        private final String name;
        private final ImageDescriptor descriptor;

        public ImageInfo(String name, ImageDescriptor descriptor) {
            this.name = name;
            this.descriptor = descriptor;
        }

        public String getName() {
            return name;
        }

        /**
         * Descriptor of latest image.
         * @return descriptor or null
         */
        public ImageDescriptor getDescriptor() {
            return descriptor;
        }
    }
}
