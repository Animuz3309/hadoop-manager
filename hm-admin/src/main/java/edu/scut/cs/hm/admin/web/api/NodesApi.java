package edu.scut.cs.hm.admin.web.api;

import edu.scut.cs.hm.admin.service.FilterService;
import edu.scut.cs.hm.admin.service.NodeStorage;
import edu.scut.cs.hm.admin.web.model.UiSearchQuery;
import edu.scut.cs.hm.admin.web.model.container.UiContainer;
import edu.scut.cs.hm.common.utils.ExtendedAssert;
import edu.scut.cs.hm.docker.DockerService;
import edu.scut.cs.hm.docker.arg.GetContainersArg;
import edu.scut.cs.hm.docker.model.container.DockerContainer;
import edu.scut.cs.hm.model.cluster.DiscoveryStorage;
import edu.scut.cs.hm.model.container.ContainerStorage;
import edu.scut.cs.hm.model.ngroup.NodesGroup;
import edu.scut.cs.hm.model.node.NodeInfo;
import edu.scut.cs.hm.model.node.NodeInfoImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/nodes", produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NodesApi {
    private final NodeStorage nodeStorage;
    private final DiscoveryStorage discoveryStorage;
    private final FilterService filterService;
    private final ContainerStorage containerStorage;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public Collection<NodeInfo> listNodes() {
        List<NodeInfo> nodes = new ArrayList<>(nodeStorage.getNodes((ni) -> true));
        nodes.replaceAll(this::prepareForUi);
        return nodes;
    }

    private NodeInfo prepareForUi(NodeInfo ni) {
        if(ni == null) {
            return null;
        }
        NodesGroup ng = discoveryStorage.getClusterForNode(ni.getName());
        String clusterName = ng == null? null : ng.getName();
        if(!Objects.equals(clusterName, ni.getCluster())) {
            ni = NodeInfoImpl.builder(ni).cluster(clusterName).build();
        }
        return ni;
    }

    @RequestMapping(value = "/{name}", method = RequestMethod.DELETE)
    public void deleteNode(@PathVariable("name") String name) {
        nodeStorage.removeNode(name);
    }

    @RequestMapping(value = "/{name}", method = RequestMethod.PUT)
    public void addNode(@PathVariable("name") String name, @RequestParam("address") String address) {
        nodeStorage.registerNode(name, address);
    }

    @RequestMapping(value = "/{name}", method = RequestMethod.GET)
    public NodeInfo getNode(@PathVariable("name") String name) {
        return prepareForUi(nodeStorage.getNodeInfo(name));
    }

    @RequestMapping(value = "/{name}/containers", method = RequestMethod.GET)
    public List<UiContainer> getContainers(@PathVariable("name") String name) {
        DockerService ds = nodeStorage.getDockerService(name);
        ExtendedAssert.notFound(ds, "Can not find docker service for node: " + name);
        List<DockerContainer> containers = ds.getContainers(new GetContainersArg(true));
        List<UiContainer> uics = containers.stream().map(UiContainer::from).collect(Collectors.toList());
        uics.forEach(c -> c.enrich(discoveryStorage, containerStorage));
        return uics;
    }

    @RequestMapping(value = "/filtered", method = RequestMethod.PUT)
    public Collection<NodeInfo> listNodes(@RequestBody UiSearchQuery searchQuery) {
        Collection<NodeInfo> nodes = listNodes();
        return filterService.listNodes(nodes, searchQuery);
    }
}
