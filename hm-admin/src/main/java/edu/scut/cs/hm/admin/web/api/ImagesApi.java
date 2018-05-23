package edu.scut.cs.hm.admin.web.api;

import com.google.common.base.Splitter;
import edu.scut.cs.hm.admin.component.ContainerSourceFactory;
import edu.scut.cs.hm.admin.component.FilterFactory;
import edu.scut.cs.hm.admin.web.model.UiUtils;
import edu.scut.cs.hm.admin.web.model.images.*;
import edu.scut.cs.hm.docker.DockerService;
import edu.scut.cs.hm.docker.arg.GetImagesArg;
import edu.scut.cs.hm.docker.arg.TagImageArg;
import edu.scut.cs.hm.docker.model.container.ContainerDetails;
import edu.scut.cs.hm.docker.model.container.DockerContainer;
import edu.scut.cs.hm.docker.model.image.*;
import edu.scut.cs.hm.docker.res.ServiceCallResult;
import edu.scut.cs.hm.model.ExtendedAssert;
import edu.scut.cs.hm.model.cluster.ClusterUtils;
import edu.scut.cs.hm.model.cluster.DiscoveryStorage;
import edu.scut.cs.hm.model.container.ContainerUtils;
import edu.scut.cs.hm.model.filter.Filter;
import edu.scut.cs.hm.model.ngroup.DockerBasedClusterConfig;
import edu.scut.cs.hm.model.ngroup.NodesGroup;
import edu.scut.cs.hm.model.node.Node;
import edu.scut.cs.hm.model.registry.ImageFilterContext;
import edu.scut.cs.hm.model.registry.RegistryRepository;
import edu.scut.cs.hm.model.registry.RegistrySearchHelper;
import edu.scut.cs.hm.model.registry.core.DockerHubRegistry;
import edu.scut.cs.hm.model.registry.core.RegistryService;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Slf4j
@RestController
@RequestMapping(value = "/api/images", produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ImagesApi {

    private static final Splitter SPLITTER = Splitter.on(",").trimResults();
    private final DiscoveryStorage discoveryStorage;
    private final RegistryRepository registryRepository;
    private final FilterFactory filterFactory;

    @RequestMapping(value = "/clusters/{cluster}/list", method = RequestMethod.GET)
    public List<ImageItem> getImages(@PathVariable("cluster") String cluster) {
        //TODO check usage of this method in CLI and if it not used - remove
        return discoveryStorage.getService(cluster).getImages(GetImagesArg.ALL);
    }

    @RequestMapping(value = "/clusters/{cluster}/deployed-list", method = RequestMethod.GET)
    public Collection<UiDeployedImage> getDeployedImages(@PathVariable("cluster") String cluster) {
        NodesGroup nodesGroup = discoveryStorage.getCluster(cluster);
        ExtendedAssert.notFound(nodesGroup, "Cluster was not found by " + cluster);
        ClusterUtils.checkClusterState(nodesGroup);
        Collection<DockerContainer> containers = nodesGroup.getContainers().getContainers();
        Map<String, UiDeployedImage> images = new HashMap<>();
        for (DockerContainer container : containers) {
            String imageId = container.getImageId();
            UiDeployedImage img = images.computeIfAbsent(imageId, UiDeployedImage::new);
            img.addContainer(container);
            loadImageTagsIfNeed(nodesGroup, container, img);
        }
        return images.values();
    }

    private void loadImageTagsIfNeed(NodesGroup service, DockerContainer container, UiDeployedImage img) {
        String imageWithTag = container.getImage();
        if (!CollectionUtils.isEmpty(img.getTags())) {
            return;
        }
        if (ImageName.isId(imageWithTag)) {
            try {
                ContainerDetails cd = service.getContainers().getContainer(container.getId());
                imageWithTag = ContainerSourceFactory.resolveImageName(cd);
                if (imageWithTag == null || ImageName.isId(imageWithTag)) {
                    return;
                }
                img.updateName(imageWithTag);
            } catch (Exception e) {
                log.error("Error while get container details:", e);
                return;
            }
        }
        String image = ImageName.withoutTag(imageWithTag);
        RegistryService registryService = registryRepository.getRegistryByImageName(image);
        if (registryService != null) {
            img.setRegistry(registryService.getConfig().getName());
            Tags tags = registryService.getTags(image);
            if (tags != null) {
                img.getTags().addAll(tags.getTags());
            }
        }
    }

    @ApiOperation("search by image substring, if you specify repository then you can use expression like '*word*' ")
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public UiSearchResult search(@RequestParam(value = "registry", required = false) String registryParam,
                                 @RequestParam(value = "cluster", required = false) String cluster,
                                 @RequestParam(value = "query", required = false) String query,
                                 @RequestParam(value = "page") int page,
                                 @RequestParam(value = "size") int size) {

        List<String> registries = new ArrayList<>();
        DockerBasedClusterConfig dcngConfig = getDockerBasedGroupsConfig(cluster);
        if (dcngConfig != null) {
            registries.addAll(dcngConfig.getConfig().getRegistries());
        }
        filterRegistries(registryParam, query, registries);

        SearchResult result;
        if (!CollectionUtils.isEmpty(registries)) {
            RegistrySearchHelper rsh = new RegistrySearchHelper(query, page, size);
            RegistryService hub = null;
            for (String registry : registries) {
                RegistryService service = registryRepository.getByName(registry);
                if (service == null) {
                    continue;
                }
                if (DockerHubRegistry.DEFAULT_NAME.equals(registry)) {
                    // we must place search results from default   registry at end
                    hub = service;
                    continue;
                }
                rsh.search(service);
            }
            if (hub != null) {
                rsh.search(hub);
            }
            result = rsh.collect();
        } else {
            result = registryRepository.search(query, page, size);
        }
        if (result == null) {
            return UiSearchResult.builder().build();
        }
        return UiSearchResult.from(result);
    }

    /**
     * when registryParam is
     * <ul>
     *   <li/>'*' - use all registries (consider that we already add al registries - we must do nothing)
     *   <li/>'' (emptystring) - use docker hub
     * </ul>
     *
     * @param registryParam null or string
     * @param query null or string
     * @param registries set of all registries
     */
    static void filterRegistries(String registryParam, String query, Collection<String> registries) {
        if("*".equals(registryParam)) {
            return;
        }
        try {
            // we may get registry name from query if param not specified
            if (registryParam == null && query != null) {
                registryParam = ContainerUtils.getRegistryPrefix(query);
            }
            // and at end we remove all registries which is not specified in param
            if (registryParam != null) {
                registries.retainAll(SPLITTER.splitToList(registryParam));
            }
        } catch (Exception e) {
            //nothing
        }
    }

    @RequestMapping(value = "/image", method = RequestMethod.GET)
    public ImageDescriptor getImage(@RequestParam("fullImageName") String fullImageName) {
        final boolean isId = ContainerUtils.isImageId(fullImageName);
        ImageDescriptor image;
        if (isId) { // id usually produced by clusters, therefore we can find it at clusters
            DockerService docker = this.discoveryStorage.getCluster(DiscoveryStorage.GROUP_ID_ALL).getDocker();
            //  not that it simply iterate over all nodes until image is appeared
            image = docker.getImage(fullImageName);
        } else {
            RegistryService registry = registryRepository.getRegistryByImageName(fullImageName);
            image = registry.getImage(fullImageName);
        }
        ExtendedAssert.notFound(image, "Can not find image: " + fullImageName);
        return image;
    }

    /**
     * Tag an image into a repository
     *
     * @param repository The repository to tag in
     * @param force      (not documented)
     */
    @RequestMapping(value = "/clusters/{cluster}/tag", method = RequestMethod.PUT)
    public ResponseEntity<?> createTag(@PathVariable("cluster") String cluster,
                                       @RequestParam(value = "imageName") String imageName,
                                       @RequestParam(value = "currentTag", defaultValue = "latest") String currentTag,
                                       @RequestParam(value = "newTag") String newTag,
                                       @RequestParam(value = "repository") String repository,
                                       @RequestParam(value = "force", required = false, defaultValue = "false") Boolean force) {
        TagImageArg tagImageArg = TagImageArg.builder()
                .force(force)
                .newTag(newTag)
                .currentTag(currentTag)
                .cluster(cluster)
                .imageName(imageName)
                .repository(repository).build();
        ServiceCallResult res = discoveryStorage.getService(cluster).createTag(tagImageArg);

        return UiUtils.createResponse(res);
    }

    @ApiOperation("get tags, filter expression is SpEL cluster image filter")
    @RequestMapping(value = "/tags", method = GET)
    public List<String> listTags(@RequestParam("imageName") String imageName,
                                 @RequestParam(value = "filter", required = false) String filter,
                                 @RequestParam(value = "cluster", required = false) String cluster) {
        Filter imageFilter = calculateImageFilter(filter, cluster);
        RegistryService registry = registryRepository.getRegistryByImageName(imageName);
        Tags tgs = registry.getTags(imageName);
        String name = registry.toRelative(imageName);
        return filter(tgs, name, registry, imageFilter);
    }

    @ApiOperation("get tags catalog (contains additional information), filter expression is SpEL cluster image filter")
    @RequestMapping(value = "/tags-detailed", method = GET)
    public List<UiTagCatalog> listTagsDetailed(@RequestParam("imageName") String imageName,
                                               @RequestParam(value = "filter", required = false) String filter,
                                               @RequestParam(value = "cluster", required = false) String cluster) {

        Filter imageFilter = calculateImageFilter(filter, cluster);
        String name = ContainerUtils.getImageNameWithoutPrefix(imageName);

        RegistryService registry = registryRepository.getRegistryByImageName(imageName);
        Tags tgs = registry.getTags(name);
        List<String> tags = filter(tgs, name, registry, imageFilter);
        return tags.stream().map(t -> {
            try {
                ImageDescriptor image = registry.getImage(name, t);
                return new UiTagCatalog(registry.getConfig().getName(), name, null, t, image != null ? image.getId() : null,
                        image != null ? image.getCreated() : null,
                        image != null ? image.getContainerConfig().getLabels() : null);
            } catch (Exception e) {
                log.error("can't download image {} / {} : {}, cause: {}", registry, name, t, e.getMessage());
                return null;
            }

        }).filter(Objects::nonNull).collect(Collectors.toList());

    }

    @ApiOperation("get images catalogs, filter expression is SpEL cluster image filter")
    @RequestMapping(value = "/", method = GET)
    public List<UiImageCatalog> listImageCatalogs(@RequestParam(value = "filter", required = false) String filterStr,
                                                  @RequestParam(value = "cluster", required = false) String cluster) {
        final Filter filter = calculateImageFilter(filterStr, cluster);
        Map<String, UiImageCatalog> catalogs = getDownloadedImages(filter);
        Collection<String> registries = registryRepository.getAvailableRegistries();
        ImageObject io = new ImageObject();

        for (String registry : registries) {
            RegistryService registryService = registryRepository.getByName(registry);
            if (!registryService.getConfig().isDisabled()) {
                String registryName = registryService.getConfig().getName();
                ImageCatalog ic = registryService.getCatalog();
                if (ic != null) {

                    for (String name : ic.getImages()) {
                        io.setName(name);
                        io.setRegistry(registryName);
                        String fullName = StringUtils.isEmpty(registryName) ? name : registryName + "/" + name;
                        io.setFullName(fullName);
                        if (!filter.test(io)) {
                            continue;
                        }
                        //we simply create uic if it absent
                        UiImageCatalog uic = catalogs.putIfAbsent(fullName, new UiImageCatalog(fullName, registryService.getConfig().getName()));
                    }
                }
            }
        }
        List<UiImageCatalog> list = new ArrayList<>(catalogs.values());
        Collections.sort(list);
        return list;
    }

    private Map<String, UiImageCatalog> getDownloadedImages(Filter filter) {
        //we can use result of this it for evaluate used space and deleting images, so need to se all images
        List<NodesGroup> nodesGroups = discoveryStorage.getClusters();
        Map<String, UiImageCatalog> catalogs = new TreeMap<>();
        for (NodesGroup nodesGroup : nodesGroups) {
            // we gather images from real clusters and orphans nodes
            String groupName = nodesGroup.getName();
            if (!ClusterUtils.isDockerBased(nodesGroup) &&
                    !DiscoveryStorage.GROUP_ID_ORPHANS.equals(groupName)) {
                continue;
            }

            try {
                processGroup(filter, catalogs, nodesGroup);
            } catch (Exception e) {
                log.error("Error while process images of \"{}\"", groupName, e);
            }
        }
        return catalogs;
    }

    private void processGroup(Filter filter, Map<String, UiImageCatalog> catalogs, NodesGroup nodesGroup) {
        ImageObject io = new ImageObject();
        GetImagesArg getImagesArg = GetImagesArg.ALL;
        List<ImageItem> images = nodesGroup.getDocker().getImages(getImagesArg);
        final String clusterName = nodesGroup.getName();
        io.setCluster(clusterName);
        //note that in some cases not all nodes of cluster have same images set, but we ignore it at this time
        final List<String> nodes = nodesGroup.getNodes().stream().map(Node::getName).collect(Collectors.toList());
        io.setNodes(nodes);
        for (ImageItem image : images) {
            for (String tag : image.getRepoTags()) {
                String imageName = ContainerUtils.getRegistryAndImageName(tag);
                if (imageName.contains(ImageName.NONE)) {
                    imageName = image.getId();
                } else {
                    io.setFullName(imageName);
                }
                if (!filter.test(io)) {
                    continue;
                }
                String registry = ContainerUtils.isImageId(imageName) ? null :
                        registryRepository.resolveRegistryNameByImageName(imageName);
                io.setRegistry(registry);
                catalogs.putIfAbsent(imageName, new UiImageCatalog(imageName, registry));
                UiImageCatalog uic = catalogs.get(imageName);
                if (!DiscoveryStorage.GROUP_ID_ORPHANS.equals(clusterName)) {
                    // we set name of real clusters only
                    uic.getClusters().add(clusterName);
                }
                String version = ContainerUtils.getImageVersion(tag);
                UiImageData imgData = uic.getOrAddId(image.getId());
                imgData.setCreated(image.getCreated());
                imgData.setSize(image.getSize());
                if (!ImageName.NONE.equals(version)) {
                    imgData.getTags().add(version);
                }
                imgData.getNodes().addAll(nodes);
            }
        }
    }

    private DockerBasedClusterConfig getDockerBasedGroupsConfig(String cluster) {
        if (StringUtils.hasText(cluster)) {
            NodesGroup nodesGroup = discoveryStorage.getCluster(cluster);
            ExtendedAssert.notFound(nodesGroup, "Cluster not found " + cluster);
            if (nodesGroup.getConfig() instanceof DockerBasedClusterConfig) {
                return (DockerBasedClusterConfig) nodesGroup.getConfig();
            }
        }
        return null;
    }

    private Filter calculateImageFilter(String filter, String cluster) {
        DockerBasedClusterConfig dbngConfig = getDockerBasedGroupsConfig(cluster);
        if (!StringUtils.hasText(filter) && dbngConfig != null) {
            filter = dbngConfig.getImageFilter();
        }
        if (!StringUtils.hasText(filter)) {
            return Filter.any();
        }
        return this.filterFactory.createFilter(filter);
    }

    private List<String> filter(Tags tags, String name, RegistryService registry, Filter filterSet) {
        if (tags == null) {
            return Collections.emptyList();
        }
        ImageFilterContext ifc = new ImageFilterContext(registry);
        ifc.setName(name);
        List<String> list = new ArrayList<>();
        for (String tag : tags.getTags()) {
            ifc.setTag(tag);
            boolean test = filterSet.test(ifc);
            if (test) {
                list.add(tag);
            }
        }
        return list;
    }

    @Data
    private static class ImageObject {
        private String name;
        private String cluster;
        private Set<String> nodes = new TreeSet<>();
        private String fullName;
        private String registry;

        void setNodes(Collection<String> nodes) {
            this.nodes.clear();
            if (nodes != null) {
                this.nodes.addAll(nodes);
            }
        }

        @Override
        public String toString() {
            return fullName;
        }

        public void clear() {
            name = null;
            cluster = null;
            nodes.clear();
            fullName = null;
            registry = null;
        }
    }
}
