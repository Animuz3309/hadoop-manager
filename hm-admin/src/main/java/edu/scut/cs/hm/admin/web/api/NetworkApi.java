package edu.scut.cs.hm.admin.web.api;

import edu.scut.cs.hm.admin.web.model.UiUtils;
import edu.scut.cs.hm.admin.web.model.network.UiNetworkBase;
import edu.scut.cs.hm.admin.web.model.network.UiNetworkCreateResult;
import edu.scut.cs.hm.admin.web.model.network.UiNetworkDetails;
import edu.scut.cs.hm.docker.cmd.CreateNetworkCmd;
import edu.scut.cs.hm.docker.cmd.DisconnectNetworkCmd;
import edu.scut.cs.hm.docker.model.network.EndpointSettings;
import edu.scut.cs.hm.docker.model.network.Network;
import edu.scut.cs.hm.docker.res.ConnectNetworkCmd;
import edu.scut.cs.hm.docker.res.CreateNetworkResponse;
import edu.scut.cs.hm.docker.res.ResultCode;
import edu.scut.cs.hm.docker.res.ServiceCallResult;
import edu.scut.cs.hm.model.ExtendedAssert;
import edu.scut.cs.hm.model.cluster.DiscoveryStorage;
import edu.scut.cs.hm.model.container.ContainerRegistration;
import edu.scut.cs.hm.model.container.ContainerStorage;
import edu.scut.cs.hm.model.ngroup.NodesGroup;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/networks", produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NetworkApi {

    private final DiscoveryStorage discoveryStorage;
    private final ContainerStorage containerStorage;

    @ApiOperation("By default make 'overlay' attachable network, prevent duplicates.")
    @RequestMapping(path = "create", method = RequestMethod.POST)
    public ResponseEntity<?> createNetwork(@RequestParam("cluster") String clusterName,
                                           @RequestParam("network") String network,
                                           @RequestBody(required = false) UiNetworkBase body) {
        NodesGroup group = getNodesGroup(clusterName);
        CreateNetworkCmd cmd = new CreateNetworkCmd();
        cmd.setName(network);
        cmd.setCheckDuplicate(true);
        cmd.setAttachable(true);
        cmd.setDriver("overlay");
        cmd.setIpam(Network.Ipam.DEF_IPAM);
        if(body != null) {
            body.to(cmd);
        }
        CreateNetworkResponse res = group.getNetworks().createNetwork(cmd);
        if(res.getCode() == ResultCode.OK) {
            UiNetworkCreateResult uincr = new UiNetworkCreateResult();
            uincr.setId(res.getId());
            String warning = res.getWarning();
            if(StringUtils.hasText(warning)) {
                uincr.setWarning(warning);
            }
            uincr.setName(network);
            return new ResponseEntity<Object>(uincr, HttpStatus.OK);
        }
        return UiUtils.createResponse(res);
    }

    @RequestMapping(path = "connect", method = RequestMethod.POST)
    public ResponseEntity<?> connectNetwork(@RequestParam("cluster") String clusterName,
                                            @RequestParam("network") String network,
                                            @RequestParam("container") String container,
                                            @RequestParam(value = "ipv4", required = false) String ipv4,
                                            @RequestParam(value = "ipv6", required = false) String ipv6) {
        NodesGroup ng = getNodesGroup(clusterName);
        ContainerRegistration cr = containerStorage.getContainer(container);
        ExtendedAssert.notFound(cr, "Can not find container with id: " + container);
        ConnectNetworkCmd cmd = new ConnectNetworkCmd();
        cmd.setContainer(container);
        cmd.setNetwork(network);
        if(ipv4 != null || ipv6 != null) {
            // we may create 'name' here, but it has no effect
            EndpointSettings config = EndpointSettings.builder()
                    .ipamConfig(EndpointSettings.EndpointIPAMConfig.builder()
                            .ipv4Address(ipv4)
                            .ipv6Address(ipv6)
                            .build())
                    .build();
            cmd.setConfig(config);
        }
        ServiceCallResult res = ng.getNetworks().connectNetwork(cmd);
        return UiUtils.createResponse(res);
    }

    @RequestMapping(path = "disconnect", method = RequestMethod.POST)
    public ResponseEntity<?> disconnectNetwork(@RequestParam("cluster") String clusterName,
                                               @RequestParam("network") String network,
                                               @RequestParam("container") String container) {
        NodesGroup ng = getNodesGroup(clusterName);
        DisconnectNetworkCmd cmd = new DisconnectNetworkCmd();
        cmd.setContainer(container);
        cmd.setNetwork(network);
        cmd.setForce(true);
        ServiceCallResult res = ng.getNetworks().disconnectNetwork(cmd);
        return UiUtils.createResponse(res);
    }


    @RequestMapping(path = "get", method = RequestMethod.GET)
    public UiNetworkDetails getNetwork(@RequestParam("cluster") String clusterName,
                                       @RequestParam("network")  String netId) {
        NodesGroup group = getNodesGroup(clusterName);
        Network net = group.getDocker().getNetwork(netId);
        ExtendedAssert.notFound(net, "Can not found network '" + netId + "' in cluster '" + clusterName + "'");
        UiNetworkDetails uinet = new UiNetworkDetails();
        uinet.setCluster(clusterName);
        return uinet.from(net, containerStorage);
    }

    @RequestMapping(path = "delete", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteNetwork(@RequestParam("cluster") String clusterName,
                                           @RequestParam("network") String network) {
        NodesGroup group = getNodesGroup(clusterName);
        ServiceCallResult res = group.getNetworks().deleteNetwork(network);
        return UiUtils.createResponse(res);
    }

    @RequestMapping(path = "delete-unused", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteUnusedNetworks(@RequestParam("cluster") String clusterName) {
        NodesGroup group = getNodesGroup(clusterName);
        ServiceCallResult res = group.getNetworks().deleteUnusedNetworks();
        return UiUtils.createResponse(res);
    }

    private NodesGroup getNodesGroup(String clusterName) {
        NodesGroup group = discoveryStorage.getCluster(clusterName);
        ExtendedAssert.notFound(group, "Cluster '" + clusterName + "' not found");
        return group;
    }
}
