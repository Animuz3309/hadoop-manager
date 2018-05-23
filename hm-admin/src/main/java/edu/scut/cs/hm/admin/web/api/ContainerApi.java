package edu.scut.cs.hm.admin.web.api;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.util.concurrent.SettableFuture;
import com.google.common.util.concurrent.Uninterruptibles;
import edu.scut.cs.hm.admin.component.ContainerSourceFactory;
import edu.scut.cs.hm.admin.service.ContainersNameService;
import edu.scut.cs.hm.admin.service.NodeStorage;
import edu.scut.cs.hm.admin.web.model.UiUtils;
import edu.scut.cs.hm.admin.web.model.container.UiContainer;
import edu.scut.cs.hm.admin.web.model.container.UiContainerDetails;
import edu.scut.cs.hm.admin.web.model.container.UiUpdateContainer;
import edu.scut.cs.hm.admin.web.model.statistics.UiStatistics;
import edu.scut.cs.hm.docker.DockerConfig;
import edu.scut.cs.hm.docker.DockerService;
import edu.scut.cs.hm.docker.arg.*;
import edu.scut.cs.hm.docker.model.container.ContainerDetails;
import edu.scut.cs.hm.docker.model.container.DockerContainer;
import edu.scut.cs.hm.docker.model.health.Statistics;
import edu.scut.cs.hm.docker.model.image.ImageDescriptor;
import edu.scut.cs.hm.docker.res.CreateAndStartContainerResult;
import edu.scut.cs.hm.docker.res.ProcessEvent;
import edu.scut.cs.hm.docker.res.ResultCode;
import edu.scut.cs.hm.docker.res.ServiceCallResult;
import edu.scut.cs.hm.model.ExtendedAssert;
import edu.scut.cs.hm.model.application.Application;
import edu.scut.cs.hm.model.application.ApplicationService;
import edu.scut.cs.hm.model.cluster.DiscoveryStorage;
import edu.scut.cs.hm.model.container.ConfigProvider;
import edu.scut.cs.hm.model.container.ContainerRegistration;
import edu.scut.cs.hm.model.container.ContainerStorage;
import edu.scut.cs.hm.model.container.ContainersManager;
import edu.scut.cs.hm.model.ngroup.NodesGroup;
import edu.scut.cs.hm.model.node.NodeInfo;
import edu.scut.cs.hm.model.node.NodeRegistration;
import edu.scut.cs.hm.model.node.NodeUtils;
import edu.scut.cs.hm.model.registry.RegistryRepository;
import edu.scut.cs.hm.model.registry.core.RegistryService;
import edu.scut.cs.hm.model.source.ContainerSource;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static edu.scut.cs.hm.model.container.ContainerUtils.getImageNameWithoutPrefix;
import static edu.scut.cs.hm.model.container.ContainerUtils.setImageVersion;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping(value = "/api/containers", produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class ContainerApi {
    private final ObjectMapper objectMapper;
    private final DiscoveryStorage discoveryStorage;
    private final RegistryRepository registryRepository;
    private final ContainersNameService containersNameService;
    private final ConfigProvider configProvider;
    private final ContainerStorage containerStorage;
    private final NodeStorage nodeStorage;
    private final ApplicationService applicationService;
    private final ContainerSourceFactory containerSourceFactory;
    private ObjectWriter objectWriter;

    @PostConstruct
    public void postConstruct() {
        this.objectWriter = objectMapper.writer().without(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
    }

    @RequestMapping(value = "/{id}/pause", method = RequestMethod.POST)
    public ResponseEntity<?> pauseContainer(@PathVariable("id") String id) {
        ContainersManager service = getContainersManager(id);
        ServiceCallResult res = service.pauseContainer(id);
        return UiUtils.createResponse(res);
    }

    @RequestMapping(value = "/{id}/unpause", method = RequestMethod.POST)
    public ResponseEntity<?> unpauseContainer(@PathVariable("id") String id) {
        ContainersManager service = getContainersManager(id);
        ServiceCallResult res = service.unpauseContainer(id);
        return UiUtils.createResponse(res);
    }

    @RequestMapping(value = "/{id}/stop", method = RequestMethod.POST)
    public ResponseEntity<?> stopContainer(@PathVariable("id") String id) {
        StopContainerArg arg = StopContainerArg.builder().id(id).build();
        ContainersManager service = getContainersManager(id);
        ServiceCallResult res = service.stopContainer(arg);
        return UiUtils.createResponse(res);
    }

    @Deprecated
    @RequestMapping(value = "/{id}/refresh", method = RequestMethod.POST)
    public ResponseEntity<?> refreshContainer(@PathVariable("id") String id) {
        StopContainerArg arg = StopContainerArg.builder().id(id).build();
        ContainersManager service = getContainersManager(id);
        ServiceCallResult resStop = service.stopContainer(arg);
        log.info("resStop {}", resStop);
        ServiceCallResult resStart = service.startContainer(id);
        return UiUtils.createResponse(resStart);
    }

    @RequestMapping(value = "/{id}/remove", method = RequestMethod.POST)
    public ResponseEntity<?> removeContainer(@PathVariable("id") String id) {
        // NOT use `getContainersManager` here!
        ContainerRegistration cr = containerStorage.getContainer(id);
        ExtendedAssert.notFound(cr, "Can not find container: " + id);
        String node = cr.getNode();
        if (node == null) {
            // container not found on any node, we must remove it from storage
            containerStorage.deleteContainer(id);
            return UiUtils.okResponse("Container '" + id + "' removed from storage. ");
        }
        NodesGroup nodesGroups = discoveryStorage.getClusterForNode(node);
        ContainersManager service = nodesGroups.getContainers();
        service.stopContainer(StopContainerArg.builder().id(id).build());
        DeleteContainerArg arg = DeleteContainerArg.builder().id(id).build();
        ServiceCallResult res = service.deleteContainer(arg);
        return UiUtils.createResponse(res);
    }

    @RequestMapping(value = "/{id}/start", method = RequestMethod.POST)
    public ResponseEntity<?> startContainer(@PathVariable("id") String id) {
        ContainersManager service = getContainersManager(id);
        ServiceCallResult res = service.startContainer(id);
        return UiUtils.createResponse(res);
    }

    @RequestMapping(value = "/{id}/restart", method = RequestMethod.POST)
    public ResponseEntity<?> restartContainer(@PathVariable("id") String id) {
        ServiceCallResult res = getContainersManager(id)
                .restartContainer(StopContainerArg.builder().id(id).build());
        return UiUtils.createResponse(res);
    }

    @RequestMapping(value = "/{id}/update", method = RequestMethod.PUT)
    public ResponseEntity<?> updateContainer(@PathVariable("id") String containerId,
                                             @RequestBody UiUpdateContainer container) {
        String cluster = getClusterForContainer(containerId);
        log.info("Begin update container '{}' at cluster: '{}' request: '{}'", containerId, cluster, container);
        NodesGroup nodesGroup = discoveryStorage.getCluster(cluster);
        ContainersManager manager = nodesGroup.getContainers();
        EditContainerArg arg = new EditContainerArg();
        arg.setContainerId(containerId);
        arg.setSource(container);
        ServiceCallResult res = manager.updateContainer(arg);
        log.info("Begin update container '{}' at cluster: '{}' result: '{}'", containerId, cluster, res);
        return UiUtils.createResponse(res);
    }

    @RequestMapping(value = "/{id}/updateLabels", method = RequestMethod.PUT)
    public void updateLabels(@PathVariable("id") String containerId,
                             Map<String, String> additionalLabels) {

        ContainerRegistration container = containerStorage.getContainer(containerId);
        ExtendedAssert.notFound(container, "Container not found by id " + containerId);
        container.setAdditionalLabels(additionalLabels);
        container.flush();
    }

    @RequestMapping(value = "/{id}/details", method = RequestMethod.GET)
    public UiContainerDetails getDetails(@PathVariable("id") String id) {
        log.info("got getDetails request id: {}", id);
        ContainerRegistration cr = containerStorage.getContainer(id);
        ExtendedAssert.notFound(cr, "Not found container: " + id);
        String node = cr.getNode();
        DockerService nodeService = (node == null) ? null : nodeStorage.getDockerService(node);
        if (nodeService != null && nodeService.isOnline()) {
            ContainerDetails container = nodeService.getContainer(id);
            return toContainerDetails(cr, container);
        }
        // it happen on containers from offline nodes and orphans
        return toContainerDetails(cr, null);
    }

    private UiContainerDetails toContainerDetails(ContainerRegistration cr, ContainerDetails container) {
        String node = cr.getNode();
        String id = cr.getId();
        UiContainerDetails res = new UiContainerDetails();
        res.setId(cr.getId());
        DockerContainer dc = cr.getContainer();
        if (container != null) {
            res.from(containerSourceFactory, container);
            res.setState(dc.getState());
        } else {
            // fallback when something wrong
            res.setName(dc.getName());
            res.setImage(dc.getImage());
            res.setImageId(dc.getImageId());
            res.setCreated(new Date(dc.getCreated()));
            res.setRun(false);
            res.setStatus(UiContainer.NO_NODE);
        }
        res.setNode(node);
        String cluster = getClusterForNode(node);
        res.setCluster(cluster);
        if (cluster != null) {
            List<Application> apps = applicationService.getApplications(cluster);
            for (Application app : apps) {
                if (app.getContainers().contains(id)) {
                    res.setApplication(app.getName());
                    break;
                }
            }
        }
        Map<String, String> additionalLabels = cr.getAdditionalLabels();
        if (additionalLabels != null) {
            res.getLabels().putAll(additionalLabels);
        }
        return res;
    }

    @RequestMapping(value = "/{id}/statistics", method = RequestMethod.GET)
    @Cacheable("UIStatistics")
    @SuppressWarnings("unchecked")
    public UiStatistics getStatistics(@PathVariable("id") String id) throws Exception {
        DockerService service = getService(id);
        log.info("got getStatistics request id: {}", id);
        GetStatisticsArg.Builder argb = GetStatisticsArg.builder();
        argb.id(id);
        argb.stream(false);
        SettableFuture<Statistics> holder = SettableFuture.create();
        argb.watcher(holder::set);
        service.getStatistics(argb.build());
        Statistics statistics = holder.get();
        return UiStatistics.from(statistics);
    }

    private DockerService getService(String id) {
        DockerService service = NodeUtils.getDockerByContainer(containerStorage, nodeStorage, id);
        ExtendedAssert.notFound(service, "Can not find container: " + id);
        return service;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public List<UiContainer> getAll() {
        List<ContainerRegistration> crs = containerStorage.getContainers();
        Map<String, String> app2cont = UiUtils.mapAppContainer(applicationService, null);
        List<UiContainer> containers = crs.stream().map((cr) -> {
            DockerContainer container = cr.getContainer();
            UiContainer uc = new UiContainer();
            if (container != null) {
                UiContainer.from(uc, container);
            } else {
                uc.setName("<invalid>");
            }
            uc.setNode(cr.getNode());
            uc.enrich(discoveryStorage, containerStorage);
            uc.setApplication(app2cont.get(uc.getId()));
            UiContainer.resolveStatus(uc, nodeStorage);
            return uc;
        }).collect(Collectors.toList());
        return UiUtils.sortAndFilterContainers(containers);
    }

    @ApiOperation("this method allows to get container's id by name and cluster")
    @RequestMapping(value = "/{cluster}/{name:.*}", method = RequestMethod.GET)
    public UiContainerDetails getContainerDetailsByName(@PathVariable("cluster") String cluster, @PathVariable("name") String name) {
        ContainerRegistration cr = containerStorage.findContainer(name);
        ExtendedAssert.notFound(cr, "Can't find container by name " + name);
        String node = cr.getNode();
        DockerService service = nodeStorage.getDockerService(node);
        ExtendedAssert.notFound(service, "Can't find container node by id " + node);
        String containerId = cr.getId();
        ContainerDetails container = service.getContainer(containerId);
        ExtendedAssert.notFound(container, "Can't find container by id " + containerId + " in node " + node);
        return toContainerDetails(cr, container);
    }

    @RequestMapping(value = "/clusters/{cluster}/defaultparams", method = GET)
    public ContainerSource defaultParams(@PathVariable("cluster") String cluster,
                                         @RequestParam("image") String image,
                                         @RequestParam("tag") String tag) {
        String fullImageName = setImageVersion(image, tag);
        RegistryService regisrty = registryRepository.getRegistryByImageName(fullImageName);
        ImageDescriptor img = regisrty.getImage(fullImageName);
        log.info("image info {}", img);
        ContainerSource res = configProvider.resolveProperties(cluster, img, getImageNameWithoutPrefix(image),
                new ContainerSource());
        DockerService dockerService = discoveryStorage.getService(cluster);

        res.setName(containersNameService.calculateName(CalcNameArg.builder()
                .allocate(false)
                .containerName(res.getName())
                .imageName(getImageNameWithoutPrefix(image))
                .dockerService(dockerService)
                .build()));

        DockerConfig clusterConfig = dockerService.getDockerConfig();
        if (!StringUtils.hasText(res.getRestart())) {
            res.setRestart(clusterConfig.getDockerRestart());
        }

        return res;
    }


    @RequestMapping(value = "/{id}/logs", method = RequestMethod.GET)
    public void getContainerLog(@PathVariable("id") String id,
                                @RequestParam(value = "stdout", defaultValue = "true", required = false) boolean stdout,
                                @RequestParam(value = "stderr", defaultValue = "true", required = false) boolean stderr,
                                @RequestParam(value = "follow", defaultValue = "false", required = false) boolean follow,
                                @RequestParam(value = "timestamps", defaultValue = "true", required = false) boolean timestamps,
                                @RequestParam(value = "since", required = false) Date since,
                                @RequestParam(value = "tail", defaultValue = "200") Integer tail,
                                final HttpServletResponse response) throws IOException {

        DockerService service = getService(id);
        try (final ServletOutputStream writer = response.getOutputStream()) {
            GetLogContainerArg arg = GetLogContainerArg.builder()
                    .id(id)
                    .tail(tail)
                    .follow(follow)
                    .stdout(stdout)
                    .stderr(stderr)
                    .timestamps(timestamps)
                    .since(since)
                    .watcher(processEvent -> {
                        // we use '\n' as delimiter for log formatter in js
                        try {
                            writer.println(processEvent.getMessage());
                            writer.flush();
                        } catch (IOException e) {
                            log.error("", e);
                        }
                    }).build();
            ServiceCallResult res = service.getContainerLog(arg);
            objectWriter.writeValue(writer, res);
        }
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void createContainer(@RequestBody ContainerSource container, final HttpServletResponse response) throws Exception {
        String node = container.getNode();
        String cluster = container.getCluster();
        if (node != null) {
            NodeInfo nodeInfo = nodeStorage.getNodeInfo(node);
            ExtendedAssert.notFound(nodeInfo, "Can not find node: " + node);
            String nodeCluster = nodeInfo.getCluster();
            if (!Objects.equals(nodeCluster, cluster)) {
                if (cluster != null) {
                    // when cluster is null we simply use node cluster
                    log.info("Node has different cluster '{}' than specified '{}', we use node cluster.", nodeCluster, cluster);
                }
                cluster = nodeCluster;
            }
        }
        NodesGroup nodeGroup = discoveryStorage.getCluster(cluster);
        ExtendedAssert.notFound(nodeGroup, "Can not find cluster: " + cluster);
        try (final ServletOutputStream writer = response.getOutputStream()) {
            createContainerImpl(new CreateContainerArg().container(container), nodeGroup, writer);
        }
    }

    private void createContainerImpl(CreateContainerArg arg,
                                     NodesGroup nodeGroup, ServletOutputStream writer) throws IOException {
        ContainerSource container = arg.getContainer();
        log.info("got create container request at cluster: {} : {}", nodeGroup.getName(), container);
        writer.println("Create container: " + container.getName() + " of " + container.getImage() + " in " + nodeGroup.getName());
        Consumer<ProcessEvent> watcher = processEvent -> {
            // we use '\n' as delimiter for log formatter in js
            try {
                writer.println(processEvent.getMessage());
                writer.flush();
            } catch (IOException e) {
                log.error("", e);
            }
        };
        try {
            arg.setWatcher(watcher);
            ProcessEvent.watch(watcher, "Creating container with params: {0}", container);
            ContainersManager containers = nodeGroup.getContainers();
            CreateAndStartContainerResult res = containers.createContainer(arg);
            ProcessEvent.watch(watcher, "Finished with {0}", res.getCode());
            objectWriter.writeValue(writer, res);
        } catch (Exception e) {
            log.error("Error during creating", e);
            objectWriter.writeValue(writer, new ServiceCallResult()
                    .code(ResultCode.ERROR)
                    .message(e.getMessage()));
        }
    }

    @RequestMapping(value = "/recreate", method = RequestMethod.POST)
    public void recreateContainer(@ApiParam("id of current container")
                                  @RequestParam("container") String containerId,
                                  final HttpServletResponse response) throws Exception {
        ContainerRegistration cr = containerStorage.getContainer(containerId);
        ExtendedAssert.notFound(cr, "Can not find container: " + containerId);
        String node = cr.getNode();
        NodesGroup nodesGroup = discoveryStorage.getClusterForNode(node);
        ExtendedAssert.notFound(nodesGroup, "Can not find cluster fro node: " + node);
        ContainersManager containers = nodesGroup.getContainers();
        ContainerDetails cd = containers.getContainer(containerId);
        ContainerSource origDetails = new ContainerSource();
        containerSourceFactory.toSource(cd, origDetails);
        origDetails.setCluster(nodesGroup.getConfig().getName());
        try (ServletOutputStream writer = response.getOutputStream()) {
            writer.println("About to delete container: " + containerId);
            ServiceCallResult delres = containers.deleteContainer(DeleteContainerArg.builder().id(containerId).kill(true).build());
            writer.println("Deleted container: " + containerId);
            objectWriter.writeValue(writer, delres);
            writer.println();
            Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);
            createContainerImpl(new CreateContainerArg().container(origDetails).enrichConfigs(true), nodesGroup, writer);
        }
    }

    private String getClusterForContainer(String containerId) {
        ContainerRegistration container = containerStorage.getContainer(containerId);
        ExtendedAssert.notFound(container, "Container \"" + containerId + "\" is not found.");
        String node = container.getNode();
        String cluster = getClusterForNode(node);
        ExtendedAssert.badRequest(cluster != null,
                "Container \"{0}\" is placed on node \"{1}\" which is not included to cluster.", containerId, node);
        return cluster;
    }

    /**
     * Return cluster of null
     *
     * @param node
     * @return
     */
    private String getClusterForNode(String node) {
        if (node == null) {
            return null;
        }
        NodeRegistration nodeReg = this.nodeStorage.getNodeRegistration(node);
        //below is not an 404, because above we found container with link to node, but cannot give existed node
        Assert.notNull(nodeReg, "Node \"" + node + "\" has invalid registration.");
        return nodeReg.getNodeInfo().getCluster();
    }

    private ContainersManager getContainersManager(String id) {
        ContainerRegistration cr = containerStorage.getContainer(id);
        ExtendedAssert.notFound(cr, "Can not find container: " + id);
        String node = cr.getNode();
        ExtendedAssert.badRequest(node != null, "Container: " + id + " nas not find on any node.");
        NodesGroup nodesGroups = discoveryStorage.getClusterForNode(node);
        return nodesGroups.getContainers();
    }
}
