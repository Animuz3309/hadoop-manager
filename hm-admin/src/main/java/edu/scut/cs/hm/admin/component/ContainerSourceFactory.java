package edu.scut.cs.hm.admin.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.scut.cs.hm.common.utils.SourceUtil;
import edu.scut.cs.hm.common.utils.StringUtils;
import edu.scut.cs.hm.common.utils.Sugar;
import edu.scut.cs.hm.docker.model.container.ContainerConfig;
import edu.scut.cs.hm.docker.model.container.ContainerDetails;
import edu.scut.cs.hm.docker.model.container.HostConfig;
import edu.scut.cs.hm.docker.model.mount.Mount;
import edu.scut.cs.hm.docker.model.mount.MountSource;
import edu.scut.cs.hm.docker.model.network.ExposedPort;
import edu.scut.cs.hm.docker.model.network.Link;
import edu.scut.cs.hm.docker.model.network.NetworkSettings;
import edu.scut.cs.hm.docker.model.network.Ports;
import edu.scut.cs.hm.docker.model.volume.VolumesFrom;
import edu.scut.cs.hm.model.cluster.SwarmUtils;
import edu.scut.cs.hm.model.container.ContainerSource;
import edu.scut.cs.hm.model.container.ContainerUtils;
import edu.scut.cs.hm.model.image.ImageName;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ContainerSourceFactory {

    private final ObjectMapper objectMapper;

    public void toSource(ContainerDetails details, ContainerSource dest) {
        convert(details, dest);
        SwarmUtils.restoreEnv(objectMapper, dest);
        SwarmUtils.clearConstraints(dest.getLabels());

    }

    @SuppressWarnings("deprecation")
    private static void convert(ContainerDetails container, ContainerSource nc) {
        nc.setId(container.getId());
        String name = ContainerUtils.fixContainerName(container.getName());
        nc.setName(name);
        ContainerConfig config = container.getConfig();
        HostConfig hostConfig = container.getHostConfig();
        nc.setBlkioWeight(hostConfig.getBlkioWeight());
        nc.setCpusetCpus(hostConfig.getCpusetCpus());
        nc.setCpuShares(hostConfig.getCpuShares());
        nc.setCpuQuota(hostConfig.getCpuQuota());
        Sugar.setIfNotNull(nc.getDns()::addAll, hostConfig.getDns());
        Sugar.setIfNotNull(nc.getDnsSearch()::addAll, hostConfig.getDnsSearch());
        Sugar.setIfNotNull(nc.getEnvironment()::addAll, config.getEnv());
        Sugar.setIfNotNull(nc.getCommand()::addAll, config.getCmd());
        Sugar.setIfNotNull(nc.getExtraHosts()::addAll, hostConfig.getExtraHosts());
        nc.setHostname(config.getHostName());
        Sugar.setIfNotNull(nc.getLabels()::putAll, config.getLabels());
        nc.getLinks().putAll(parseLinks(hostConfig.getLinks()));
        if (hostConfig.getMemory() != null && hostConfig.getMemory() > 0) {
            nc.setMemoryLimit(hostConfig.getMemory());
        }
        nc.setNetwork(hostConfig.getNetworkMode());
        if (container.getNode() != null) {
            nc.setNode(container.getNode().getName());
        }
        NetworkSettings networkSettings = container.getNetworkSettings();
        if (networkSettings != null && networkSettings.getNetworks() != null) {
            nc.getNetworks().addAll(networkSettings.getNetworks().keySet());
        }
        Ports portBindings = hostConfig.getPortBindings();

        nc.getPorts().putAll(parsePorts(portBindings));
        nc.setVolumeDriver(hostConfig.getVolumeDriver());
        VolumesFrom[] volumesFrom = hostConfig.getVolumesFrom();
        if(volumesFrom != null) {
            Arrays.stream(volumesFrom).forEach(vf -> nc.getVolumesFrom().add(vf.toString()));
        }
        nc.setVolumeDriver(hostConfig.getVolumeDriver());
        nc.getVolumeBinds().addAll(hostConfig.getBinds());
        Set<String> ignored = hostConfig.getBinds().stream().map(s -> StringUtils.afterLast(s, ':')).collect(Collectors.toSet());
        loadMounts(container.getMounts(), hostConfig.getMounts(), nc.getMounts(), ignored);

        //TODO            createContainerArg.setLogging(hostConfig.getLogConfig()); and etc
        Sugar.setIfNotNull(nc.getSecurityOpt()::addAll, hostConfig.getSecurityOpts());

        nc.setImage(resolveImageName(container));
        nc.setImageId(container.getImageId());
    }

    private static void loadMounts(List<ContainerDetails.MountPoint> srcPoints, List<Mount> srcMounts,
                                   List<MountSource> dest, Set<String> ignored) {
        log.info("srcPoints: {}, srcMounts:{}, ignored: {}", srcPoints, srcMounts, ignored);
        // docker place all mounts to MountPoints, but this does not provide enough info
        // otherwise hostConfig.mounts have full info but sometime may be empty, therefore we use both sources
        Set<String> converted = new HashSet<>();
        if(srcMounts != null) {
            srcMounts.stream()
                    .filter(m -> !m.isSystem())
                    .filter(m -> !ignored.contains(m.getTarget()))
                    .forEach(m -> {
                        MountSource ms = SourceUtil.toMountSource(m);
                        if (ms != null) {
                            converted.add(ms.getTarget());
                            dest.add(ms);
                        }
                    });
        }
        if(srcPoints != null) {
            srcPoints.stream()
                    .filter(m -> !m.isSystem())
                    .filter(m -> !ignored.contains(m.getDestination()))
                    .filter(p -> !converted.contains(p.getDestination())) // we prevent appearing same mount from multiple source
                    .filter(p -> p.getType() != null)
                    .forEach(p -> {
                        MountSource ms = SourceUtil.toMountSource(p);
                        if (ms != null) {
                            dest.add(ms);
                        }
                    });
        }
        log.info("result mounts: {}", dest);
    }

    public static String resolveImageName(ContainerDetails container) {
        String imageName = container.getImage();
        if(ImageName.isId(imageName)) {
            ContainerConfig config = container.getConfig();
            if(config != null && config.getImage() != null) {
                imageName = config.getImage();
            }
        }
        return imageName;
    }

    private static Map<String, String> parseLinks(List<Link> links) {
        if(links == null) {
            return Collections.emptyMap();
        }
        return links.stream().collect(Collectors.toMap(Link::getAlias, Link::getName));
    }

    private static Map<String, String> parsePorts(Ports portBindings) {
        if (portBindings != null) {
            Map<String, String> map = new HashMap<>();
            Map<ExposedPort, Ports.Binding[]> bindings = portBindings.getBindings();
            for (Map.Entry<ExposedPort, Ports.Binding[]> exposedPortEntry : bindings.entrySet()) {
                ExposedPort key = exposedPortEntry.getKey();
                Ports.Binding[] value = exposedPortEntry.getValue();
                for (Ports.Binding binding : value) {
                    map.put(Integer.toString(key.getPort()), binding.getHostPortSpec());
                }
            }
            return map;
        } else {
            return Collections.emptyMap();
        }
    }
}

