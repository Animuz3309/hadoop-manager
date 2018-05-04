package edu.scut.cs.hm.model.cluster;

import com.google.common.net.InetAddresses;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import edu.scut.cs.hm.admin.component.ContainerCreator;
import edu.scut.cs.hm.admin.security.TempAuth;
import edu.scut.cs.hm.admin.service.DiscoveryStorageImpl;
import edu.scut.cs.hm.admin.service.NodeStorage;
import edu.scut.cs.hm.common.utils.AddressUtils;
import edu.scut.cs.hm.common.utils.RescheduledTask;
import edu.scut.cs.hm.common.utils.SingleValueCache;
import edu.scut.cs.hm.docker.DockerService;
import edu.scut.cs.hm.docker.DockerServiceInfo;
import edu.scut.cs.hm.docker.arg.RemoveNodeArg;
import edu.scut.cs.hm.docker.arg.SwarmLeaveArg;
import edu.scut.cs.hm.docker.cmd.SwarmJoinCmd;
import edu.scut.cs.hm.docker.cmd.UpdateNodeCmd;
import edu.scut.cs.hm.docker.model.swarm.SwarmInfo;
import edu.scut.cs.hm.docker.model.swarm.SwarmNode;
import edu.scut.cs.hm.docker.res.ResultCode;
import edu.scut.cs.hm.docker.res.ServiceCallResult;
import edu.scut.cs.hm.model.container.ContainerStorage;
import edu.scut.cs.hm.model.container.ContainersManager;
import edu.scut.cs.hm.model.ngroup.AbstractNodesGroup;
import edu.scut.cs.hm.model.ngroup.DockerClusterConfig;
import edu.scut.cs.hm.model.ngroup.NodesGroup;
import edu.scut.cs.hm.model.node.NodeInfo;
import edu.scut.cs.hm.model.node.NodeRegistration;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A kind of nodegroup which is managed by 'docker' in 'swarm mode'.
 */
@Slf4j
public class DockerCluster extends AbstractNodesGroup<DockerClusterConfig> {

    /**
     * List of cluster manager nodes.
     * key -> cluster name
     * value -> manager node of the docker cluster
     */
    private final Map<String, Manager> managers = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduledExecutor;
    private final SingleValueCache<ClusterData> data = SingleValueCache.builder(() -> {
        DockerService docker = getDockerOrNull();
        if(docker == null) {
            return null;
        }
        return loadClusterData(docker);
    })
    .nullStrategy(SingleValueCache.NullStrategy.DIRTY)
    .timeAfterWrite(Long.MAX_VALUE)// we cache for always, but must invalidate it at cluster reinitialization
    .build();

    private final SingleValueCache<Map<String, SwarmNode>> nodesMap;
    private final RescheduledTask rereadNodesTask;
    private ContainerStorage containerStorage;
    private ContainerCreator containerCreator;
    private ContainersManager containers;
    private int rereadNodesTimeout;
    private volatile List<AutoCloseable> closeables;

    DockerCluster(DiscoveryStorageImpl storage, DockerClusterConfig config) {
        super(config, storage, Collections.singleton(Feature.SWARM_MODE));

        long cacheTimeAfterWrite = config.getConfig().getCacheTimeAfterWrite();
        nodesMap = SingleValueCache.builder(this::loadNodesMap)
                .timeAfterWrite(cacheTimeAfterWrite)
                .nullStrategy(SingleValueCache.NullStrategy.DIRTY)
                .build();

        this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat(getClass().getSimpleName() + "-" + getName() + "-%d")
                .build());

        this.rereadNodesTask = RescheduledTask.builder()
                .runnable(this::rereadNodes)
                .service(this.scheduledExecutor)
                .maxDelay(10L, TimeUnit.SECONDS)
                .build();
    }

    /**
     * @see ClusterFactory#build(String)
     * @param rereadNodesTimeout
     */
    // 注意此处动态注入
    @Autowired
    void setRereadNodesTimeout(@Value(SwarmUtils.EXPR_NODES_UPDATE)int rereadNodesTimeout) {
        this.rereadNodesTimeout = rereadNodesTimeout;
    }

    /**
     * @see ClusterFactory#build(String)
     * @param containerStorage
     */
    // 注意此处动态注入
    @Autowired
    void setContainerStorage(ContainerStorage containerStorage) {
        this.containerStorage = containerStorage;
    }

    /**
     * @see ClusterFactory#build(String)
     * @param containerCreator
     */
    // 注意此处动态注入
    @Autowired
    void setContainerCreator(ContainerCreator containerCreator) {
        this.containerCreator = containerCreator;
    }

    private void rereadNodes() {
        try(TempAuth au = TempAuth.asSystem()) {
            // nodes in the actual docker swarm cluster
            Map<String, SwarmNode> factNodes = nodesMap.get();
            if (factNodes == null) {
                log.error("Cluster {}: can not load map of cluster nodes.", getName());
                return;
            }
            ClusterData clusterData = data.get();
            if (clusterData == null) {
                log.warn("Cluster {}: can not reread node, because cluster data is null.", getName());
                return;
            }

            // flag which mean that a change internal cluster node list, and must reread them
            boolean[] modified = new boolean[]{false};

            // nodes in the cluster but maybe not in the actual docker swarm managed cluster
            Map<String, NodeInfo> registeredNodes = new HashMap<>();
            getNodeStorage().forEach(nr -> {
                NodeInfo ni = nr.getNodeInfo();
                if (!this.isFromSameCluster(ni)) {
                    return;
                }
                registeredNodes.put(ni.getName(), ni);
            });

            //  nodes in cluster which is down (to docker swarm) to rejoin in the actual docker swarm managed cluster
            Map<String, SwarmNode> localMap = factNodes;
            registeredNodes.forEach((name, ni) -> {
                SwarmNode sn = localMap.get(name);
                if (sn != null && sn.getStatus().getState() != SwarmNode.NodeState.DOWN) {
                    return;
                }
                if (sn == null && ownedByAnotherCluster(name)) {
                    return;
                }
                // notify system that node is not connected to cluster
                updateNodeRegistration(name, getNodeAddress(sn), sn);

                // down node may mean that it simply leave from cluster but not removed, we must try to join it
                Manager manager = managers.get(name);
                if (manager != null) {
                    joinAsManager(manager, clusterData);
                } else {
                    joinAsWorker(name, clusterData);
                }
                modified[0] = true;
            });

            // add nodes which is not in cluster but in the actual docker swarm managed cluster
            factNodes.forEach((name, sn) -> {
                SwarmNode.State status = sn.getStatus();
                String address = getNodeAddress(sn);
                if(StringUtils.isEmpty(address) ||
                        status.getState() != SwarmNode.NodeState.READY ||
                        registeredNodes.containsKey(name)) {
                    return;
                }
                registerNode(name, address);
                modified[0] = true;
            });

            if (modified[0]) {
                // we touch some 'down' nodes and must reload list for new status
                nodesMap.invalidate();
                factNodes = nodesMap.get();
            }

            if(factNodes == null) {
                log.error("Can not load map of cluster nodes.");
                return;
            }

            factNodes.forEach((name, sn) -> {
                rereadNode(sn);
            });
        } catch (Exception e) {
            log.error("Can not update list of nodes due to error.", e);
        }
    }

    /**
     * Leave node from current cluster. It do multiple calls to different docker services.
     * @param node name of node
     * @param sn swarm node object
     */
    private void leave(String node, SwarmNode sn) {
        DockerService clusterDocker = getDocker();
        leave(clusterDocker, node, sn);
    }

    /**
     * Lave node from current cluster. It do multiple calls to different docker services.
     * @param manager manager service
     * @param node name of node
     * @param sn swarm node object
     */
    private void leave(DockerService manager, String node, SwarmNode sn) {
        log.info("Begin leave node '{}' from '{}'", node, getName());
        final String id = sn.getId();
        if(isManager(sn)) {
            UpdateNodeCmd un = new UpdateNodeCmd();
            un.setVersion(sn.getVersion().getIndex());
            un.setNodeId(id);
            un.setRole(UpdateNodeCmd.Role.WORKER);
            un.setAvailability(UpdateNodeCmd.Availability.DRAIN);
            ServiceCallResult scr = manager.updateNode(un);
            log.info("Demote manager node '{}' with result: '{}'", node, scr);
        }
        DockerService nodeDocker = getNodeStorage().getDockerService(node);
        if(nodeDocker == null) {
            log.warn("Can not leave node '{}' from cluster, node does not have registered docker service", node);
            return;
        } else {
            ServiceCallResult res = nodeDocker.leaveSwarm(new SwarmLeaveArg());
            log.info("Result of leave node '{}' : {} {}", node, res.getCode(), res.getMessage());
        }
        ServiceCallResult rmres = manager.removeNode(new RemoveNodeArg(id).force(true));
        log.info("Result of remove node '{}' from cluster: {} {}", node, rmres.getCode(), rmres.getMessage());
    }

    private boolean isManager(SwarmNode sn) {
        return sn.getManagerStatus() != null;
    }

    private String getNodeName(SwarmNode sn) {
        return sn.getDescription().getHostname();
    }

    private void rereadNode(SwarmNode sn) {
        String nodeName = getNodeName(sn);
        String address = getNodeAddress(sn);
        if(StringUtils.isEmpty(address)) {
            log.warn("Node {} does not contain address, it is usual for docker prior to 1.13 version.", nodeName);
            return;
        }
        try {
            if (InetAddresses.forString(sn.getStatus().getAddress()).isLoopbackAddress()) {
                // local node address it a wrong config, or user simply use single-node cluster - we can not detect
                // this, and just report it
                log.warn("Node {} report local address '{}', it may be wrong configuration.", nodeName, address);
            }
        } catch(Exception e) {
            log.warn("Node {} report wrong address '{}', it parsed with error:", nodeName, address, e);
        }
        NodeRegistration nr = updateNodeRegistration(nodeName, address, sn);
        if(!Objects.equals(getName(), nr.getCluster())) {
            log.info("Node {} is from another cluster: '{}', we remove it from our cluster: '{}'.", nodeName, nr.getCluster(), getName());
            leave(nodeName, sn);
        }
    }

    private void registerNode(String node, String address) {
        try {
            NodeStorage nodes = getNodeStorage();
            nodes.registerNode(node, address);
        } catch (Exception e) {
            log.error("While register node '{}' at '{}'", node, address, e);
        }
    }

    /**
     * Join as manager
     * @param manager
     * @param clusterData
     */
    private void joinAsManager(Manager manager, ClusterData clusterData) {
        log.info("Begin join manager node '{}' to '{}'", manager.name, getName());
        DockerService ds = manager.getService();
        if(ds == null) {
            log.warn("Can not join master node '{}', it does not have registered docker service", manager.name);
            return;
        }
        if(!ds.isOnline()) {
            log.warn("Can not join node '{}', it offline", manager.name);
            return;
        }

        String masterToken = clusterData.getManagerToken();
        SwarmJoinCmd cmd = new SwarmJoinCmd();
        cmd.setToken(masterToken);
        cmd.getManagers().addAll(clusterData.getManagers());
        String swarmAddress = getSwarmAddress(ds);
        cmd.setListen(swarmAddress);
        try {
            ServiceCallResult res = ds.joinSwarm(cmd);
            log.info("Result of joining node '{}': {} {}", manager.name, res.getCode(), res.getMessage());
        } catch (RuntimeException e) {
            log.error("Can not join node '{}' due to error: ", manager.name, e);
        }
    }

    /**
     * Join node as worker to this docker cluster
     * @param name name of node
     */
    private void joinAsWorker(String name, ClusterData clusterData) {
        //join to swarm
        log.info("Begin join node '{}' to '{}'", name, getName());
        String workerToken = clusterData.getWorkerToken();
        DockerService ds = getNodeStorage().getDockerService(name);
        if(ds == null) {
            log.warn("Can not join node '{}', it does not have registered docker service", name);
            return;
        }
        if(!ds.isOnline()) {
            log.warn("Can not join node '{}', it offline", name);
            return;
        }

        SwarmJoinCmd cmd = new SwarmJoinCmd();
        cmd.setToken(workerToken);
        this.managers.forEach((k, v) -> cmd.getManagers().addAll(clusterData.getManagers()));
        cmd.setListen(getSwarmAddress(ds));

        try {
            ServiceCallResult res = ds.joinSwarm(cmd);
            // http 503
            if (res.getStatus() == HttpStatus.SERVICE_UNAVAILABLE) {
                DockerServiceInfo dsi = ds.getInfo();
                SwarmInfo swarm = dsi.getSwarm();
                if (swarm != null) {
                    if(swarm.isManager() && dsi.getNodeCount() > 1) {
                        // we must not leave manager, when it has at least one additional node
                        log.error("Error: node '{}' is manager of another cluster, wa can not join it.", name);
                    } else {
                        //node already join to another cluster and need leave
                        // in cases when node join to existed cluster we must not leave it, therefore we not to using 'force' flag
                        SwarmLeaveArg sla = new SwarmLeaveArg();
                        if(swarm.isManager()) {
                            // be careful with this option, it may broke cluster (and we must not use it on
                            // managers with worker nodes)
                            sla.setForce(true);
                        }
                        ServiceCallResult lr = ds.leaveSwarm(sla);
                        if(lr.getCode() == ResultCode.OK) {
                            // try again
                            res = ds.joinSwarm(cmd);
                        }
                    }
                }
            }
            log.info("Result of joining node '{}': {} {}", name, res.getCode(), res.getMessage());
        } catch (RuntimeException e) {
            log.error("Can not join node '{}' due to error: ", name, e);
        }
    }

    /**
     * Return address with swarm port, it differ from usual http port and use some binary protocol.
     * @see DockerClusterConfig#getSwarmPort()
     * @param service service of node
     * @return address with swarm port
     */
    private String getSwarmAddress(DockerService service) {
        String host = AddressUtils.getHost(service.getAddress());
        return AddressUtils.setPort(host, config.getSwarmPort());
    }

    /**
     * Retrieve node address. Note that node may report incorrect address (for example 127.0.0.1), therefore we
     * must not prefer it over manually entered value (in other words - not replace existed address).
     * @param sn swarm node object
     * @return string with host and port
     */
    private String getNodeAddress(SwarmNode sn) {
        if (sn == null) {
            return null;
        }
        String address = sn.getStatus().getAddress();
        if (StringUtils.isEmpty(address)) {
            return address;
        }
        if (!AddressUtils.hasPort(address)) {
            // docker default port 2375
            address = AddressUtils.setPort(address, 2375);
        }
        return address;
    }

    /**
     * Update node registration. Note that method must work when address and SwarmNode is null
     * @param nodeName name
     * @param address address or null
     * @param sn swarm node object or null
     * @return non null registration
     */
    private NodeRegistration updateNodeRegistration(String nodeName, String address, SwarmNode sn) {
        // todo
        return null;
    }

    private boolean isFromSameCluster(NodeInfo ni) {
        return ni != null && (this.managers.containsKey(ni.getName()) ||
                getName().equals(ni.getCluster()));
    }

    private DockerService getDockerOrNull() {
        //todo
        return null;
    }

    private ClusterData loadClusterData(DockerService manager) {
        //todo
        return null;
    }

    private Map<String, SwarmNode> loadNodesMap() {
        //todo
        return null;
    }

    private boolean ownedByAnotherCluster(String nodeName) {
        NodesGroup nodeCluster = getDiscoveryStorage().getClusterForNode(nodeName);
        if(nodeCluster == null || nodeCluster == this) {
            return false;
        }
        //we can not use node from another cluster, for prevent broke it
        log.warn("Can not use node '{}' of '{}' cluster, node already used in existed '{}' cluster.",
                nodeName, DockerCluster.this.getName(), nodeCluster.getName());
        return true;
    }

    @Data
    @lombok.Builder(builderClassName = "Builder")
    private static class ClusterData {
        private final String workerToken;
        private final String managerToken;
        private final List<String> managers;
    }

    /**
     * Manager node of DockerCluster
     */
    private final class Manager {
        private final String name;
        private DockerService service;

        Manager(String name) {
            this.name = name;
        }

        synchronized DockerService getService() {
            loadService();
            return service;
        }

        private synchronized void loadService() {
            if (service != null) {
                return;
            }
            NodeStorage nodeStorage = getNodeStorage();
            NodeRegistration nr = nodeStorage.getNodeRegistration(name);
            if (nr == null) {
                return;
            }
            String thisCluster = DockerCluster.this.getName();
            if (!thisCluster.equals(nr.getCluster())) {
                if (ownedByAnotherCluster(name)) {
                    // we can not use this service, because it not our cluster
                    return;
                }
                // in some cases node may has different cluster, it cause undefined behaviour
                // therefore we must force node to new cluster
                nodeStorage.setNodeCluster(name, thisCluster);
            }
            service = nr.getDocker();
        }
    }
}
