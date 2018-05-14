package edu.scut.cs.hm.admin.web.api;

import edu.scut.cs.hm.admin.security.AccessContext;
import edu.scut.cs.hm.admin.security.AccessContextFactory;
import edu.scut.cs.hm.admin.security.SecuredType;
import edu.scut.cs.hm.admin.service.FilterService;
import edu.scut.cs.hm.admin.service.SourceService;
import edu.scut.cs.hm.admin.service.NodeStorage;
import edu.scut.cs.hm.admin.web.model.UiPermission;
import edu.scut.cs.hm.admin.web.model.cluster.UiCluster;
import edu.scut.cs.hm.common.utils.ExtendedAssert;
import edu.scut.cs.hm.docker.DockerConfig;
import edu.scut.cs.hm.model.application.Application;
import edu.scut.cs.hm.model.application.ApplicationService;
import edu.scut.cs.hm.model.cluster.DiscoveryStorage;
import edu.scut.cs.hm.model.container.ContainerStorage;
import edu.scut.cs.hm.model.ngroup.DockerBasedClusterConfig;
import edu.scut.cs.hm.model.ngroup.DockerClusterConfig;
import edu.scut.cs.hm.model.ngroup.NodesGroup;
import edu.scut.cs.hm.model.node.NodeInfo;
import edu.scut.cs.hm.model.registry.RegistryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@Slf4j
@RequestMapping(value = "/api/clusters", produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ClustersApi {

    private final RegistryRepository registryRepository;

    private final NodeStorage nodeStorage;
    private final SourceService sourceService;
    private final DiscoveryStorage discoveryStorage;
    private final ApplicationService applicationService;
    private final ContainerStorage containerStorage;
    private final FilterService filterService;
    private final AccessContextFactory aclContextFactory;

    @RequestMapping(value = "/", method = GET)
    public List<UiCluster> listClusters() {
        AccessContext ac = aclContextFactory.getContext();
        Collection<NodesGroup> clusters = this.discoveryStorage.getClusters();
        List<UiCluster> ucs = clusters.stream().map(c -> this.toUi(ac, c)).collect(Collectors.toList());
        ucs.sort(Comparator.naturalOrder());
        return ucs;
    }

    @RequestMapping(value = { "/{cluster}" }, method = GET)
    public UiCluster getCluster(@PathVariable("cluster") String cluster) {
        AccessContext ac = aclContextFactory.getContext();
        NodesGroup nodesGroup = discoveryStorage.getCluster(cluster);
        ExtendedAssert.notFound(nodesGroup, "Cluster was not found by " + cluster);
        return toUi(ac, nodesGroup);
    }

    private UiCluster toUi(AccessContext ac, NodesGroup cluster) {
        UiCluster uc = new UiCluster();

        //================= base info ===================================
        final String name = cluster.getName();
        uc.setName(name);
        uc.getTitle().accept(cluster.getTitle());
        uc.getDescription().accept(cluster.getDescription());
        uc.getFilter().accept(cluster.getImageFilter());
        uc.setFeatures(cluster.getFeatures());

        //================= docker config ===================================
        if (cluster.getConfig() instanceof DockerBasedClusterConfig) {
            DockerBasedClusterConfig cfg = (DockerBasedClusterConfig) cluster.getConfig();
            uc.setConfig(DockerConfig.builder(cfg.getConfig()));
            if(cfg instanceof DockerClusterConfig) {
                uc.setManagers(((DockerClusterConfig)cfg).getManagers());
            }
        }

        UiCluster.Entry containersEntry = new UiCluster.Entry();
        UiCluster.Entry nodeEntry = new UiCluster.Entry();

        //================= nodes and containers ===================================
        try {
            List<NodeInfo> nodes = cluster.getNodes();
            nodes.forEach(ni -> {
                if(ni.isOn() && ni.getHealth().getHealthy()) {
                    nodeEntry.incrementOn();
                } else {
                    nodeEntry.incrementOff();
                }
            });
            cluster.getContainers().getContainers().forEach(dc -> {
                if(dc.isRun()) {
                    containersEntry.incrementOn();
                } else {
                    containersEntry.incrementOff();
                }
            });
        } catch (Exception e) {
            //nothing
        }
        uc.setContainers(containersEntry);
        uc.setNodes(nodeEntry);

        //================= applications ===================================
        try {
            Set<String> apps = uc.getApplications();
            List<Application> applications = applicationService.getApplications(name);
            applications.forEach(a -> apps.add(a.getName()));
        } catch (Exception e) {
            //nothing
        }

        //================= cluster state and permission ===================================
        uc.setState(cluster.getState());
        UiPermission.inject(uc, ac, SecuredType.CLUSTER.id(name));
        return uc;
    }

}
