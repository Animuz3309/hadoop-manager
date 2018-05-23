package edu.scut.cs.hm.admin.web.api;

import edu.scut.cs.hm.admin.security.AccessContext;
import edu.scut.cs.hm.admin.security.AccessContextFactory;
import edu.scut.cs.hm.admin.security.SecuredType;
import edu.scut.cs.hm.admin.service.FilterService;
import edu.scut.cs.hm.admin.service.SourceService;
import edu.scut.cs.hm.admin.service.NodeStorage;
import edu.scut.cs.hm.admin.web.model.UiPermission;
import edu.scut.cs.hm.admin.web.model.UiSearchQuery;
import edu.scut.cs.hm.admin.web.model.UiUtils;
import edu.scut.cs.hm.admin.web.model.cluster.UiCluster;
import edu.scut.cs.hm.admin.web.model.cluster.UiClusterEditablePart;
import edu.scut.cs.hm.admin.web.model.container.UiContainer;
import edu.scut.cs.hm.admin.web.model.container.UiContainerService;
import edu.scut.cs.hm.admin.web.model.network.UiNetwork;
import edu.scut.cs.hm.common.security.Authorities;
import edu.scut.cs.hm.docker.DockerServiceInfo;
import edu.scut.cs.hm.docker.DockerUtils;
import edu.scut.cs.hm.docker.arg.NodeUpdateArg;
import edu.scut.cs.hm.docker.model.container.DockerContainer;
import edu.scut.cs.hm.docker.model.network.Network;
import edu.scut.cs.hm.model.ExtendedAssert;
import edu.scut.cs.hm.docker.DockerConfig;
import edu.scut.cs.hm.model.HttpException;
import edu.scut.cs.hm.model.application.Application;
import edu.scut.cs.hm.model.application.ApplicationService;
import edu.scut.cs.hm.model.cluster.ClusterUtils;
import edu.scut.cs.hm.model.cluster.DiscoveryStorage;
import edu.scut.cs.hm.model.container.ContainerService;
import edu.scut.cs.hm.model.container.ContainerStorage;
import edu.scut.cs.hm.model.ngroup.*;
import edu.scut.cs.hm.model.node.NodeInfo;
import edu.scut.cs.hm.model.registry.RegistryRepository;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

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


    @Secured({Authorities.ADMIN_ROLE, SecuredType.CLUSTER_ADMIN})
    @RequestMapping(value = "/{cluster}", method = DELETE)
    public void deleteCluster(@PathVariable("cluster") String cluster) {
        discoveryStorage.deleteCluster(cluster);
    }

    @Secured({Authorities.ADMIN_ROLE, SecuredType.CLUSTER_ADMIN})
    @RequestMapping(value = "/{cluster}", method = PUT)
    public void createCluster(@PathVariable("cluster") String name, @RequestBody(required = false) UiClusterEditablePart data) {
        log.info("about to create cluster: [{}], {}", name, data);
        AtomicBoolean flag = new AtomicBoolean(false);// we can not use primitives in closure
        NodesGroup cluster = discoveryStorage.getOrCreateCluster(name, (ccc) -> {
            String type = null;
            if (data != null) {
                type = data.getType();
            }
            if (type == null) {
                type = NodesGroupConfig.TYPE_SWARM;
            }
            AbstractNodesGroupConfig<?> gc = ccc.createConfig(type);
            if(data != null) {
                DockerConfig.Builder config = data.getConfig();
                if(gc instanceof DockerBasedClusterConfig && config != null) {
                    config.setCluster(name); // we must fix name of cluster
                    ((DockerBasedClusterConfig)gc).setConfig(config.build());
                }
                data.toCluster(gc);
            }
            ccc.setMustValidated(true);
            flag.set(true);
            return gc;
        });
        if(!flag.get()) {
            throw new HttpException(HttpStatus.NOT_MODIFIED, "Cluster '" + name + "' is already exists.");
        }
        log.info("Cluster created: {}", cluster);
        cluster.flush();
    }

    @Secured({Authorities.ADMIN_ROLE, SecuredType.CLUSTER_ADMIN})
    @RequestMapping(value = "/{cluster}", method = PATCH)
    public void updateCluster(@PathVariable("cluster") String name, @RequestBody UiClusterEditablePart data) {
        log.info("Begin update cluster: [{}], {}", name, data);
        NodesGroup cluster = discoveryStorage.getCluster(name);
        ExtendedAssert.notFound(cluster, "can not find cluster: " + name);
        cluster.updateConfig((cc) -> {
            if(cc instanceof DockerBasedClusterConfig) {
                DockerBasedClusterConfig dbcc = (DockerBasedClusterConfig) cc;
                DockerConfig.Builder ccib = DockerConfig.builder(dbcc.getConfig());
                ccib.merge(data.getConfig());
                // update config
                dbcc.setConfig(ccib.build());
            }
            data.toCluster(cc);
        });
        if(!cluster.getState().isInited()) {
            cluster.init();
        }
        log.info("Cluster updated: {}", cluster);
    }

    @RequestMapping(value = "/{cluster}/containers", method = GET)
    public ResponseEntity<Collection<UiContainer>> listContainers(@PathVariable("cluster") String cluster) {
        return new ResponseEntity<>(fetchContainers(cluster), HttpStatus.OK);
    }

    private Collection<UiContainer> fetchContainers(String cluster) {
        AccessContext ac = aclContextFactory.getContext();
        NodesGroup nodesGroup = discoveryStorage.getCluster(cluster);
        ExtendedAssert.notFound(nodesGroup, "Cluster was not found by " + cluster);
        if (nodesGroup.getState().isOk()) {
            List<UiContainer> list = new ArrayList<>();
            Collection<DockerContainer> containers = nodesGroup.getContainers().getContainers();
            Map<String, String> apps = UiUtils.mapAppContainer(applicationService, nodesGroup);
            for (DockerContainer container : containers) {
                UiContainer uic = UiContainer.from(container);
                uic.enrich(discoveryStorage, containerStorage);
                uic.setApplication(apps.get(uic.getId()));
                UiContainer.resolveStatus(uic, nodeStorage);
                UiPermission.inject(uic, ac, SecuredType.CONTAINER.id(uic.getId()));
                list.add(uic);
            }
            return UiUtils.sortAndFilterContainers(list);
        }
        return Collections.emptyList();
    }

    @RequestMapping(value = "/{cluster}/containers", method = PUT)
    public ResponseEntity<Collection<UiContainer>> filteredListContainers(@PathVariable("cluster") String cluster,
                                                                          @RequestBody UiSearchQuery searchQuery) {
        Collection<UiContainer> containers = fetchContainers(cluster);
        Collection<UiContainer> uiContainers = filterService.listNodes(containers, searchQuery);
        return new ResponseEntity<>(uiContainers, HttpStatus.OK);
    }

    @RequestMapping(value = "/{cluster}/services", method = GET)
    public ResponseEntity<Collection<UiContainerService>> listServices(@PathVariable("cluster") String cluster) {
        AccessContext ac = aclContextFactory.getContext();
        NodesGroup nodesGroup = discoveryStorage.getCluster(cluster);
        ExtendedAssert.notFound(nodesGroup, "Cluster was not found by " + cluster);
        ClusterUtils.checkClusterState(nodesGroup);
        Collection<ContainerService> services = nodesGroup.getContainers().getServices();
        Map<String, String> apps = UiUtils.mapAppContainer(applicationService, nodesGroup);
        List<UiContainerService> list = new ArrayList<>();
        for (ContainerService service : services) {
            UiContainerService uic = UiContainerService.from(nodesGroup, service);
            uic.setApplication(apps.get(uic.getId()));
            UiPermission.inject(uic, ac, SecuredType.SERVICE.id(uic.getId()));
            list.add(uic);
        }
        Collections.sort(list);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @RequestMapping(value = "/{cluster}/info", method = GET)
    public DockerServiceInfo info(@PathVariable("cluster") String cluster) {
        return discoveryStorage.getService(cluster).getInfo();
    }

    @RequestMapping(value = "/{cluster}/nodes-detailed", method = GET)
    public Collection<NodeInfo> listNodesDetailed(@PathVariable("cluster") String cluster) {
        return getNodesInternal(cluster);
    }

    private Collection<NodeInfo> getNodesInternal(String clusterName) {
        NodesGroup cluster = discoveryStorage.getCluster(clusterName);
        ExtendedAssert.notFound(cluster, "Can not find cluster: " + clusterName);
        return cluster.getNodes();
    }

    @RequestMapping(value = "/{cluster}/nodes", method = GET)
    public List<String> listNodes(@PathVariable("cluster") String cluster) {
        return DockerUtils.listNodes(getNodesInternal(cluster));
    }

    @Secured(Authorities.ADMIN_ROLE)
    @ApiOperation("Add node to specified cluster. Node must be present in same environment wit cluster.")
    @RequestMapping(value = "/{cluster}/nodes/{node}", method = POST)
    public ResponseEntity<?> addNode(@PathVariable("cluster") String clusterId, @PathVariable("node") String node) {
        // we setup cluster
        NodesGroup cluster = discoveryStorage.getOrCreateCluster(clusterId, null);
        //and then attach node to it
        if (cluster.getFeatures().contains(NodesGroup.Feature.FORBID_NODE_ADDITION)) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "Cluster: " + clusterId + " does not allow addition of nodes.");
        }
        NodeInfo ni = nodeStorage.setNodeCluster(node, clusterId);
        return new ResponseEntity<>(Objects.equals(clusterId, ni.getCluster())? HttpStatus.OK : HttpStatus.CONFLICT);
    }

    @ApiOperation("Remove node from specified cluster. Also you can use 'all' cluster or any other - node will be correctly removed anyway.")
    @RequestMapping(value = "/{cluster}/nodes/{node}", method = DELETE)
    public ResponseEntity<?> removeNode(@PathVariable("cluster") String clusterId, @PathVariable("node") String node) {
        NodeInfo ni = nodeStorage.setNodeCluster(node, null);
        return new ResponseEntity<>(ni.getCluster() == null? HttpStatus.OK : HttpStatus.CONFLICT);
    }

    @RequestMapping(value = "/{cluster}/nodes/{node}/update", method = POST)
    public ResponseEntity<?> updateNode(@PathVariable("cluster") String clusterId,
                                        @PathVariable("node") String node,
                                        @RequestBody NodeUpdateArg body) {
        body.setNode(node);
        NodesGroup ng = discoveryStorage.getCluster(clusterId);
        ExtendedAssert.notFound(ng, "Cluster was not found by " + clusterId);
        return UiUtils.createResponse(ng.updateNode(body));
    }

    @RequestMapping(value = "/{cluster}/registries", method = GET)
    public List<String> getRegistriesForCluster(@PathVariable("cluster") String cluster) {
        Collection<String> availableRegistries = registryRepository.getAvailableRegistries();
        NodesGroup nodesGroup = discoveryStorage.getCluster(cluster);
        List<String> registries = new ArrayList<>();
        ExtendedAssert.notFound(nodesGroup, "Cluster was not found by " + cluster);
        if (nodesGroup.getConfig() instanceof DockerBasedClusterConfig) {
            DockerBasedClusterConfig ngc = (DockerBasedClusterConfig) nodesGroup.getConfig();
            registries.addAll(ngc.getConfig().getRegistries());
        }
        registries.retainAll(availableRegistries);
        return registries;
    }


    @RequestMapping(path = "/{cluster}/networks", method = RequestMethod.GET)
    public List<UiNetwork> getNetworks(@PathVariable("cluster") String clusterName) {
        NodesGroup group = discoveryStorage.getCluster(clusterName);
        ExtendedAssert.notFound(group, "Can not find cluster: " + clusterName);
        ClusterUtils.checkClusterState(group);
        Map<String, Network> networks = group.getNetworks().getNetworks();
        ArrayList<UiNetwork> results = new ArrayList<>(networks.size());
        networks.forEach((id, src) -> {
            UiNetwork res = new UiNetwork();
            res.from(src, containerStorage);
            res.setCluster(clusterName);
            results.add(res);
        });
        results.sort(null);
        return results;
    }
}
