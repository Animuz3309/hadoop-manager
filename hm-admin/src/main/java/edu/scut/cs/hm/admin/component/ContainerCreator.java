package edu.scut.cs.hm.admin.component;

import com.google.common.base.MoreObjects;
import edu.scut.cs.hm.common.utils.Consumers;
import edu.scut.cs.hm.common.utils.ExtendedAssert;
import edu.scut.cs.hm.common.utils.SourceUtil;
import edu.scut.cs.hm.docker.DockerService;
import edu.scut.cs.hm.docker.DockerUtils;
import edu.scut.cs.hm.docker.arg.CalcNameArg;
import edu.scut.cs.hm.docker.arg.CreateContainerArg;
import edu.scut.cs.hm.docker.cmd.CreateContainerCmd;
import edu.scut.cs.hm.docker.model.container.ContainerDetails;
import edu.scut.cs.hm.docker.model.container.HostConfig;
import edu.scut.cs.hm.docker.model.container.RestartPolicy;
import edu.scut.cs.hm.docker.model.image.ImageDescriptor;
import edu.scut.cs.hm.docker.model.network.ExposedPort;
import edu.scut.cs.hm.docker.model.network.ExposedPorts;
import edu.scut.cs.hm.docker.model.network.Ports;
import edu.scut.cs.hm.docker.res.*;
import edu.scut.cs.hm.model.cluster.DiscoveryStorage;
import edu.scut.cs.hm.model.cluster.SwarmUtils;
import edu.scut.cs.hm.model.container.*;
import edu.scut.cs.hm.model.image.ImageName;
import edu.scut.cs.hm.model.ngroup.NodesGroup;
import edu.scut.cs.hm.model.node.NodeInfo;
import edu.scut.cs.hm.model.node.NodeRegistry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.google.common.base.MoreObjects.firstNonNull;
import static edu.scut.cs.hm.common.utils.StringUtils.before;
import static edu.scut.cs.hm.docker.DockerUtils.SCALABLE;
import static edu.scut.cs.hm.docker.model.container.RestartPolicy.parse;

/**
 * Manager for containers, it use DockerService as backend, but do something things
 * which is not provided by docker out of box.
 */
@Slf4j
@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Data
public class ContainerCreator {

    private static final int CREATE_CONTAINER_TRIES = 3;
    private final DiscoveryStorage discoveryStorage;
    private final NodeRegistry nodeRegistry;            // @see NodeStorage
    private final ConfigProvider configProvider;
    private final ContainersNameService containersNameService;
    private final ContainerStorage containerStorage;
    private final ContainerSourceFactory containerSourceFactory;

    /**
     * Create container by image information (image name, tag) also can be specified optional params <p/>
     * <b>Must not throw any exception after start creation of container.</b>
     * @param arg argument
     * @param docker ngroup or node service
     * @return id of new container
     */
    public CreateAndStartContainerResult createContainer(CreateContainerArg arg, DockerService docker) {
        CreateContainerContext cc = new CreateContainerContext(arg, docker);
        return createContainerInternal(cc);
    }

    /**
     * add scalable to doc
     * @param docker swarm service
     * @param scaleFactor
     * @param id
     * @return resul
     */
    public ServiceCallResult scale(DockerService docker, Integer scaleFactor, String id) {
        ContainerDetails container = docker.getContainer(id);
        String cluster = docker.getCluster();
        Assert.notNull(cluster, "Wrong docker service: " + docker + " it must be swarm service.");
        ExtendedAssert.notFound(container, "Can not find container: " + id);
        String scalable = container.getConfig().getLabels().get(SCALABLE);
        if (scalable == null || "true".equals(scalable)) {
            int scale = scaleFactor == null ? 1 : scaleFactor;
            for (int i = 0; i < scale; i++) {
                ContainerSource nc = new ContainerSource();
                containerSourceFactory.toSource(container, nc);
                nc.setCluster(cluster);
                nc.setNode(null);
                nc.setName(null);
                nc.setHostname(null);
                nc.setDomainname(null);
                SwarmUtils.clearLabels(nc.getLabels());
                CreateContainerArg arg = new CreateContainerArg().container(nc);
                CreateContainerContext cc = new CreateContainerContext(arg, docker);
                CreateAndStartContainerResult containerInternal = createContainerInternal(cc);
                if (containerInternal.getCode() == ResultCode.ERROR) {
                    return containerInternal;
                }
            }
            return new ServiceCallResult()
                    .code(ResultCode.OK).message("Created " + scale + " instances");
        } else {
            return new ServiceCallResult()
                    .code(ResultCode.ERROR)
                    .message("Image not scalable " + container.getConfig().getImage());
        }
    }

    private CreateAndStartContainerResult createContainerInternal(CreateContainerContext cc) {
        CreateAndStartContainerResult result = new CreateAndStartContainerResult();
        try {
            CreateContainerResponse response = createWithTries(cc);
            final String containerId = response.getId();
            result.setContainerId(containerId);
            ServiceCallResult startRes = cc.dockerService.startContainer(containerId);
            ResultCode code = startRes.getCode();
            if (code != ResultCode.OK) {
                log.error("Start container '{}' was failed.", startRes.getMessage());
            }
            result.setCode(code);
            result.setMessage(startRes.getMessage());
            if (result.getContainerId() != null) {
                ContainerDetails container = cc.dockerService.getContainer(containerId);
                if (container != null) {
                    String node = container.getNode() == null ? null : container.getNode().getName();
                    if(node == null) {
                        log.error("Container '{}' has null node.", containerId);
                    }
                    container.setName(ContainerUtils.fixContainerName(container.getName()));
                    ContainerRegistration orCreateContainer = containerStorage.updateAndGetContainer(container, node);
                    orCreateContainer.setAdditionalLabels(cc.arg.getContainer().getLabels());
                } else {
                    log.error("Can't receive container '{}' from service.", containerId);
                }
            }
        } catch (Exception e) {
            log.error("Can't create container", e);
            result.setCode(ResultCode.ERROR);
            result.setMessage(e.getMessage());
        }
        // name previously set in this.buildCreateContainer(),
        //   and we need name in for cleanup container if this will be failed at start
        result.setName(cc.getName());
        return result;
    }

    // we try create container, if we got conflict, then try again with other name
    private CreateContainerResponse createWithTries(CreateContainerContext cc) {

        int tries = CREATE_CONTAINER_TRIES;
        CreateContainerResponse response = null;
        while (tries > 0) {
            tries--;
            response = doCreation(cc);
            if (response.getCode() == ResultCode.OK) {
                return response;
            }
            boolean weCanTryAgain = response.getCode() == ResultCode.CONFLICT &&
                    !StringUtils.hasText(cc.arg.getContainer().getName());
            if (!weCanTryAgain) {
                break;
            }
        }
        if (ResultCode.OK != response.getCode()) {
            throw new IllegalStateException("Can't create container, due: " + response.getCode() + " " + response.getMessage());
        }
        return response;
    }

    private CreateContainerResponse doCreation(CreateContainerContext cc) {
        CreateContainerCmd cmd = buildCreateContainer(cc);
        CreateContainerResponse response = cc.dockerService.createContainer(cmd);
        ProcessEvent.watch(cc.watcher, "Result of execution: {0}", response);
        return response;
    }

    protected CreateContainerCmd buildCreateContainer(CreateContainerContext cc) {
        DockerService dockerService = cc.dockerService;
        ContainerSource nc = cc.arg.getContainer();
        ImageDescriptor image = getImage(cc);
        String imageName = resolveImageName(nc);
        ContainerSource result = nc;
        List<String> currentEnvironment = new ArrayList<>(nc.getEnvironment());
        nc.getEnvironment().clear();
        if (cc.arg.isEnrichConfigs()) {
            result = configProvider.resolveProperties(nc.getCluster(), image, imageName, nc);
        }
        Map<String, Integer> appCountPerNode = getContainersPerNodeForImage(cc, imageName);
        List<String> existsNodes = DockerUtils.listNodes(dockerService.getInfo().getNodeList());
        // we want to save order of entries, but skip duplicates
        LinkedHashSet<String> env = new LinkedHashSet<>(result.getEnvironment());
        env.addAll(currentEnvironment);
        ContainerStarterHelper.calculateConstraints(existsNodes,
                result.getNode(),
                appCountPerNode,
                dockerService.getDockerConfig().getMaxCountOfInstances(), env);
        log.info("Env: {}", env);
        ProcessEvent.watch(cc.watcher, "Environment: {0}", env);

        String name = containersNameService.calculateName(CalcNameArg.builder()
                .allocate(true)
                .containerName(result.getName())
                .imageName(imageName)
                .dockerService(dockerService).build());
        cc.setName(name);
        ProcessEvent.watch(cc.watcher, "Calculated name of the container: {0}", name);
        log.info("Calculated name of the container {}", name);

        CreateContainerCmd cmd = new CreateContainerCmd();
        cmd.setName(name);
        cmd.setHostName(MoreObjects.firstNonNull(nc.getHostname(), name));
        cmd.setDomainName(nc.getDomainname());
        cmd.setEnv(filterEnv(env));
        cmd.setImage(imageName);
        cmd.setLabels(result.getLabels());
        cmd.getLabels().put(ContainerUtils.LABEL_IMAGE_NAME, imageName);
        cmd.setCmd(convertAndFilter(nc.getCommand()));
        cmd.setEntrypoint(convertAndFilter(nc.getEntrypoint()));
        cmd.setHostConfig(getHostConfig(cc, result));
        Ports portBindings = cmd.getHostConfig().getPortBindings();
        if (portBindings != null && !CollectionUtils.isEmpty(portBindings.getPorts())) {
            Map<ExposedPort, Ports.Binding[]> bindings = portBindings.getBindings();
            cmd.setExposedPorts(new ExposedPorts(bindings.keySet()));
        }
        log.info("Command for execution: {}", cmd);
        ProcessEvent.watch(cc.watcher, "Command for execution: {0}", cmd);
        return cmd;
    }

    private String[] filterEnv(Collection<String> env) {
        log.info("All env {}", env);
        Set<String> filter = new HashSet<>();
        String[] constraints = env.stream()
                .filter(e -> e.contains("="))
                .filter(e -> {
                    String before = before(e, '=');
                    if (before.contains("constraint")) {
                        return true;
                    }
                    return filter.add(before(e, '='));
                }).toArray(String[]::new);
        log.info("Filtered env {}", Arrays.toString(constraints));
        return constraints;
    }

    private ImageDescriptor getImage(CreateContainerContext cc) {
        DockerService dockerService = cc.dockerService;
        ContainerSource nc = cc.arg.getContainer();
        ImageDescriptor image = null;
        String imageId = resolveImageId(nc);
        String imageName = resolveImageName(nc);
        if(ImageName.isId(imageId)) {
            // we can not pull images by id, and must try to find its on nodes
            image = dockerService.getImage(imageId);
        }
        if(image == null && !ImageName.isId(imageName)) {
            // pulling of images by its id is not supported
            image = dockerService.pullImage(imageName, cc.watcher);
        }
        Assert.notNull(image, "Can not resolve image from imageName=" + imageName + ", imageId=" + imageId + " on service=" + dockerService.getId());
        return image;
    }

    private static String resolveImageName(ContainerSource nc) {
        String name = ContainerUtils.getFixedImageName(nc);
        if(name == null) {
            //getFixedImageName skip getImage value when it is Id
            name = MoreObjects.firstNonNull(nc.getImageId(), nc.getImage());
        }
        return name;
    }

    private static String resolveImageId(ContainerSource nc) {
        String imageId = nc.getImageId();
        if(imageId == null) {
            ImageName in = ImageName.parse(nc.getImage());
            if(in != null) {
                imageId = in.getId();
            }
        }
        return imageId;
    }

    private String[] convertAndFilter(List<String> strings) {
        if (CollectionUtils.isEmpty(strings)) {
            return null;
        }
        List<String> collect = strings.stream().filter(StringUtils::hasText).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(collect)) {
            return null;
        }
        return collect.toArray(new String[collect.size()]);
    }

    private Map<String, Integer> getContainersPerNodeForImage(CreateContainerContext cc, String imageName) {
        //first we need to resolve use we cluster or single node
        String cluster = cc.arg.getContainer().getCluster();
        DockerService service = null;
        if (cluster != null) {
            service = getDockerForCluster(cluster);
        }
        if (service == null) {
            service = cc.dockerService;
        }
        Map<String, Integer> map = new HashMap<>();
        for (NodeInfo ni : service.getInfo().getNodeList()) {
            String nodeName = ni.getName();
            List<ContainerRegistration> containers = containerStorage.getContainersByNode(ni.getName());
            if(CollectionUtils.isEmpty(containers)) {
                continue;
            }
            int count = (int) containers.stream().filter(c -> imageName.equals(c.getContainer().getImage())).count();
            map.put(nodeName, count);
        }
        return map;
    }

    private HostConfig getHostConfig(CreateContainerContext cc, ContainerSource arg) {

        Long mem = arg.getMemoryLimit();
        RestartPolicy restartPolicy = getRestartPolicy(cc, arg);
        List<String> hostBindings = getHostBindings(arg);
        Set<String> bindedTargets = getBindedTargets(hostBindings);
        HostConfig.HostConfigBuilder builder = HostConfig.builder()
                .memory(mem)
                .blkioWeight(arg.getBlkioWeight())
                .cpuQuota(arg.getCpuQuota())
                .cpuShares(arg.getCpuShares())
                .binds(hostBindings)
                .portBindings(getBindings(arg.getPorts()))
                .publishAllPorts(arg.isPublishAllPorts())
                .restartPolicy(restartPolicy);

        builder.mounts(arg.getMounts().stream()
                .filter(m -> !bindedTargets.contains(m.getTarget()))
                .map(SourceUtil::fromMountSource).collect(Collectors.toList()));
        makeNetwork(cc, arg, builder);
        return builder.build();
    }

    private Set<String> getBindedTargets(List<String> hostBindings) {
        Set<String> binds = new HashSet<>();
        for (String binding : hostBindings) {
            String[] arr = StringUtils.delimitedListToStringArray(binding, ":");
            if (arr.length > 1) {
                String target = arr[1];
                // remove trailing slashes
                while(target.endsWith("/") && target.length() > 1) {
                    target = target.substring(0, target.length() - 1);
                }
                binds.add(target);
            }
        }
        return binds;
    }

    private void makeNetwork(CreateContainerContext cc, ContainerSource arg, HostConfig.HostConfigBuilder b) {
        String networkSrc = arg.getNetwork();
        List<String> networksSrc = arg.getNetworks();
        // also we need make support of multiply 'networks'
        if(networkSrc == null) {
            String cluster = arg.getCluster();
            if(cluster != null) {
                NodesGroup ng = discoveryStorage.getCluster(cluster);
                if (ng != null) {
                    b.networkMode(ng.getDefaultNetworkName());
                    return;
                }
                log.warn("Cluster \"{}\" does not have any network, so container \"{}\" will be created with default network.", cluster, cc.getName());
            }
        } else {
            b.networkMode(networkSrc);
        }
    }

    private RestartPolicy getRestartPolicy(CreateContainerContext cc, ContainerSource arg) {
        String restartPolicyString;
        if (arg.getRestart() != null) {
            restartPolicyString = arg.getRestart();
        } else {
            restartPolicyString = cc.dockerService.getDockerConfig().getDockerRestart();
        }
        //we can define default policy in future
        return restartPolicyString == null ? RestartPolicy.noRestart() : parse(restartPolicyString);
    }

    private List<String> getHostBindings(ContainerSource arg) {
        List<String> volumeBinds = arg.getVolumeBinds();
        if(volumeBinds == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(volumeBinds);
    }

    private DockerService getDockerForCluster(String clusterId) {
        return discoveryStorage.getService(clusterId);
    }

    private Ports getBindings(Map<String, String> publish) {
        Ports ports = new Ports();
        if (publish != null) {
            for (String key : publish.keySet()) {
                if (StringUtils.hasText(key)) {
                    String value = publish.get(key);
                    ExposedPort exposedPort = new ExposedPort(Integer.parseInt(value));
                    Ports.Binding binding = Ports.Binding.parse(key);
                    ports.bind(exposedPort, binding);
                }
            }
        }
        return ports;
    }

    private class CreateContainerContext {
        final CreateContainerArg arg;
        final Consumer<ProcessEvent> watcher;
        /**
         * Service instance of concrete node or ngroup on which do creation .
         */
        final DockerService dockerService;
        private String name;

        CreateContainerContext(CreateContainerArg arg, DockerService service) {
            this.arg = arg;
            Assert.notNull(arg.getContainer(), "arg.container is null");
            this.watcher = firstNonNull(arg.getWatcher(), Consumers.<ProcessEvent>nop());
            if (service != null) {
                this.dockerService = service;
            } else {
                this.dockerService = getDocker(arg);
            }
        }

        private DockerService getDocker(CreateContainerArg arg) {
            //we create container only on node, ignore swarm and virtual service
            String node = arg.getContainer().getNode();
            Assert.hasText(node, "Node is null or empty");
            DockerService service = nodeRegistry.getDockerService(node);
            Assert.notNull(service, "Can not fins service for node: " + node);
            return service;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
