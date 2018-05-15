package edu.scut.cs.hm.admin.service;

import edu.scut.cs.hm.admin.component.ContainerSourceFactory;
import edu.scut.cs.hm.common.kv.KeyValueStorage;
import edu.scut.cs.hm.common.kv.KvUtils;
import edu.scut.cs.hm.common.kv.mapping.KvMap;
import edu.scut.cs.hm.common.kv.mapping.KvMapperFactory;
import edu.scut.cs.hm.common.mb.MessageBus;
import edu.scut.cs.hm.model.ExtendedAssert;
import edu.scut.cs.hm.docker.DockerService;
import edu.scut.cs.hm.docker.arg.StopContainerArg;
import edu.scut.cs.hm.docker.model.container.ContainerDetails;
import edu.scut.cs.hm.docker.res.CreateApplicationResult;
import edu.scut.cs.hm.model.application.Application;
import edu.scut.cs.hm.model.application.ApplicationEvent;
import edu.scut.cs.hm.model.application.ApplicationImpl;
import edu.scut.cs.hm.model.application.ApplicationService;
import edu.scut.cs.hm.model.cluster.DiscoveryStorage;
import edu.scut.cs.hm.model.compose.ComposeArg;
import edu.scut.cs.hm.model.container.ContainersManager;
import edu.scut.cs.hm.model.ngroup.NodesGroup;
import edu.scut.cs.hm.model.source.ApplicationSource;
import edu.scut.cs.hm.model.source.ContainerSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

// todo add docker compose support
@Slf4j
@Service
public class ApplicationServiceImpl implements ApplicationService {

    private final KeyValueStorage keyValueStorage;
    private final DiscoveryStorage discoveryStorage;
    private final String appPrefix;

    private final MessageBus<ApplicationEvent> applicationBus;
    private final ContainerSourceFactory sourceService;
    private final KvMap<ApplicationImpl> map;

    @Autowired
    public ApplicationServiceImpl(KvMapperFactory mapper,
                                  DiscoveryStorage discoveryStorage,
                                  ContainerSourceFactory sourceService,
                                  @Qualifier(ApplicationEvent.BUS) MessageBus<ApplicationEvent> applicationBus) {
        this.keyValueStorage = mapper.getStorage();
        this.appPrefix = keyValueStorage.getPrefix() + "/applications/";
        this.map = KvMap.builder(ApplicationImpl.class)
                .mapper(mapper)
                .path(this.appPrefix)
                .build();
        this.discoveryStorage = discoveryStorage;
        this.applicationBus = applicationBus;
        this.sourceService = sourceService;
    }


    @Override
    public List<Application> getApplications(String cluster) {
        List<String> appKeys = keyValueStorage.list(appPrefix + cluster);
        if(appKeys == null) {
            return Collections.emptyList();
        }
        List<Application> apps = new ArrayList<>();
        appKeys.forEach((k) -> {
            String name = KvUtils.suffix(appPrefix, k);
            // name has 'cluster/appName'
            ApplicationImpl app = map.get(name);
            apps.add(app);
        });
        return apps;
    }

    @Override
    public void startApplication(String cluster, String id) throws Exception {
        NodesGroup service = discoveryStorage.getCluster(cluster);
        Application application = getApplication(cluster, id);

        if (application.getInitFile() != null) {
            // todo starting using compose, also checks new versions
        } else {
            // starting manually
            ContainersManager containers = service.getContainers();
            application.getContainers().forEach(containers::startContainer);
        }
    }

    @Override
    public CreateApplicationResult deployCompose(ComposeArg composeArg) throws Exception {
        return null;
    }

    @Override
    public void stopApplication(String cluster, String id) {
        Application application = getApplication(cluster, id);

        ContainersManager service = getService(cluster);
        application.getContainers().forEach(c -> service.stopContainer(StopContainerArg.builder().id(c).build()));
    }

    private ContainersManager getService(String cluster) {
        NodesGroup service = discoveryStorage.getCluster(cluster);
        return service.getContainers();
    }

    @Override
    public Application getApplication(String cluster, String id) {
        ApplicationImpl applicationInstance = readApplication(cluster, id);
        ExtendedAssert.notFound(applicationInstance, "application was not found " + id);
        ContainersManager service = getService(cluster);
        ApplicationImpl.Builder clone = ApplicationImpl.builder().from(applicationInstance);
        List<String> existedContainers = applicationInstance.getContainers().stream()
                .filter(c -> service.getContainer(c) != null).collect(Collectors.toList());
        return clone.containers(existedContainers).build();
    }

    private ApplicationImpl readApplication(String cluster, String appId) {
        return map.get(buildKey(cluster, appId));
    }

    private String buildKey(String cluster, String id) {
        return cluster + "/" + id;
    }

    @Override
    public ApplicationSource getSource(String cluster, String id) {
        Application application = getApplication(cluster, id);
        ContainersManager service = getService(cluster);
        ApplicationSource src = new ApplicationSource();
        src.setName(application.getName());
        application.getContainers().stream()
                .map(c -> {
                    ContainerDetails cd = service.getContainer(c);
                    ContainerSource conf = new ContainerSource();
                    sourceService.toSource(cd, conf);
                    conf.setApplication(id);
                    conf.setCluster(cluster);
                    conf.getLabels().put(APP_LABEL, id);
                    return conf;
                })
                .forEach(src.getContainers()::add);
        return src;
    }

    @Override
    public File getInitComposeFile(String cluster, String appId) {
        // todo with compose
        return null;
    }

    @Override
    public void addApplication(Application application) {
        Assert.notNull(application, "application can't be null");
        String appName = application.getName();
        ExtendedAssert.matchId(appName, "application name");

        ContainersManager service = getService(application.getCluster());
        List<String> containers = application.getContainers();
        List<String> existedContainers = containers.stream().filter(c -> service.getContainer(c) != null).collect(Collectors.toList());
        Assert.isTrue(!CollectionUtils.isEmpty(existedContainers), "Application doesn't have containers " + application);
        String key = buildKey(application.getCluster(), appName);
        map.put(key, ApplicationImpl.from(application));
    }

    @Override
    public void removeApplication(String cluster, String id) {
        log.info("about to remove application: {}, in cluster: {}", id, cluster);
        Application application = getApplication(cluster, id);
        DockerService service = discoveryStorage.getService(application.getCluster());
        // todo need to remove application add by compose
        map.remove(buildKey(cluster, id));
    }
}
