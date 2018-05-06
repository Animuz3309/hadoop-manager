package edu.scut.cs.hm.model.cluster;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.net.InetAddresses;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import edu.scut.cs.hm.admin.component.ContainerCreator;
import edu.scut.cs.hm.admin.config.configurer.DockerConfigurer;
import edu.scut.cs.hm.admin.security.TempAuth;
import edu.scut.cs.hm.admin.service.DiscoveryStorageImpl;
import edu.scut.cs.hm.admin.service.NodeStorage;
import edu.scut.cs.hm.common.utils.AddressUtils;
import edu.scut.cs.hm.common.utils.Closeables;
import edu.scut.cs.hm.common.utils.RescheduledTask;
import edu.scut.cs.hm.common.utils.SingleValueCache;
import edu.scut.cs.hm.docker.DockerService;
import edu.scut.cs.hm.docker.DockerServiceInfo;
import edu.scut.cs.hm.docker.arg.NodeUpdateArg;
import edu.scut.cs.hm.docker.arg.RemoveNodeArg;
import edu.scut.cs.hm.docker.arg.SwarmLeaveArg;
import edu.scut.cs.hm.docker.cmd.SwarmInitCmd;
import edu.scut.cs.hm.docker.cmd.SwarmJoinCmd;
import edu.scut.cs.hm.docker.cmd.UpdateNodeCmd;
import edu.scut.cs.hm.docker.model.swarm.JoinTokens;
import edu.scut.cs.hm.docker.model.swarm.SwarmInfo;
import edu.scut.cs.hm.docker.model.swarm.SwarmSpec;
import edu.scut.cs.hm.docker.res.SwarmInitResult;
import edu.scut.cs.hm.docker.res.SwarmInspectResponse;
import edu.scut.cs.hm.docker.model.swarm.SwarmNode;
import edu.scut.cs.hm.docker.res.ResultCode;
import edu.scut.cs.hm.docker.res.ServiceCallResult;
import edu.scut.cs.hm.model.container.ContainerStorage;
import edu.scut.cs.hm.model.container.ContainersManager;
import edu.scut.cs.hm.model.container.DockerClusterContainers;
import edu.scut.cs.hm.model.ngroup.AbstractNodesGroup;
import edu.scut.cs.hm.model.ngroup.DockerClusterConfig;
import edu.scut.cs.hm.model.ngroup.NodesGroup;
import edu.scut.cs.hm.model.node.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.*;

/**
 * A kind of nodegroup which is managed by 'docker' in 'swarm mode'.
 */
@Slf4j
public class DockerCluster extends AbstractNodesGroup<DockerClusterConfig> {

    /**
     * @param id name of node, when we add a node use the name
     * @return
     */
    @Override
    public boolean hasNode(String id) {
        NodeRegistration nr = getNodeStorage().getNodeRegistration(id);
        return isFromSameCluster(nr);
    }

    /**
     * Return copy of all current nodes collection
     *
     * @return copy of current nodes
     */
    @Override
    public List<NodeInfo> getNodes() {
        Map<String, SwarmNode> map = getState().isOk()?  nodesMap.get() : Collections.emptyMap();
        ImmutableList.Builder<NodeInfo> b = ImmutableList.builder();
        getNodeStorage().forEach(nr -> {
            NodeInfo ni = nr.getNodeInfo();
            SwarmNode sn = map.get(ni.getName());
            if(!isFromSameCluster(ni) && sn == null) {
                return;
            }
            NodeMetrics.State state =(sn != null)? getState(sn) : NodeMetrics.State.ALONE;
            NodeMetrics metrics = ni.getHealth();
            if(metrics.getState() != state) {
                NodeMetrics.Builder nmb = NodeMetrics.builder().from(metrics);
                setNodeState(nmb, state);
                ni = NodeInfoImpl.builder(ni)
                        .health(nmb.build())
                        .build();
            }
            b.add(ni);
        });
        return b.build();
    }

    /**
     * Update node but not only attributes of physical node, some 'node in docker swarm ngroup' attr
     *
     * @param arg
     * @return
     */
    @Override
    public ServiceCallResult updateNode(NodeUpdateArg arg) {
        final String node = arg.getNode();
        Assert.hasText(node, "arg.node is null or empty");
        Assert.notNull(node, "Can not find node with name:" + node);

        NodeRegistration nr = getNodeStorage().getNodeRegistration(node);
        NodeInfo nodeInfo = nr.getNodeInfo();

        UpdateNodeCmd cmd = new UpdateNodeCmd();
        cmd.setNodeId(nodeInfo.getIdInCluster());
        cmd.setVersion(arg.getVersion());
        cmd.setLabels(arg.getLabels());
        // availability is required, so we set default value
        cmd.setAvailability(MoreObjects.firstNonNull(arg.getAvailability(), UpdateNodeCmd.Availability.ACTIVE));
        cmd.setRole(arg.getRole());
        DockerService docker = getDocker();

        ServiceCallResult res = docker.updateNode(cmd);
        if(res.getCode() == ResultCode.OK) {
            scheduleRereadNodes();
        }

        return res;
    }

    /**
     * Empty Set
     *
     * @return
     */
    @Override
    public Collection<String> getGroups() {
        return Collections.emptySet();
    }

    /**
     * When we use swarm or 'docker in swarm mode' we return manager node's docker service
     *
     * @return
     */
    @Override
    public DockerService getDocker() {
        DockerService ds = getDockerOrNull();
        if(ds == null) {
            throw new IllegalStateException("Cluster " + getName() + " has not any alive manager node.");
        }
        return ds;
    }

    /**
     * Tool for managing ngroup containers, it replace for direct access to docker service
     *
     * @return non null value
     */
    @Override
    public ContainersManager getContainers() {
        return containers;
    }

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
     * @param nodeStorageConfig {@link edu.scut.cs.hm.admin.config.DockerConfiguration#nodeStorageConfig(DockerConfigurer)}
     */
    // 注意此处动态注入
    @Autowired
    void setRereadNodesTimeout(NodeStorageConfig nodeStorageConfig) {
        this.rereadNodesTimeout = nodeStorageConfig.getUpdateSeconds();
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


    @Override
    protected void initImpl() {
        List<String> hosts = this.config.getManagers();
        if (CollectionUtils.isEmpty(hosts)) {
            cancelInit("config must contains at least one manager host.");
            return;
        }
        hosts.forEach(host -> managers.putIfAbsent(host, new Manager(host)));
        initCluster(hosts.get(0));

        if (getStateCode() == S_INITING) {
            ArrayList<AutoCloseable> closeables = new ArrayList<>();
            this.closeables = closeables;
            // we do this only if initialization process is success
            this.containers = new DockerClusterContainers(this, this.containerStorage, this.containerCreator);

            // so docker does not send any events about new coming nodes, and we must refresh list of them
            ScheduledFuture<?> sf = this.scheduledExecutor.scheduleWithFixedDelay(rereadNodesTask::schedule,
                    rereadNodesTimeout, rereadNodesTimeout, TimeUnit.SECONDS);
            closeables.add(() -> sf.cancel(true));
            closeables.add(getNodeStorage().getNodeEventSubscriptions().openSubscription(this::onNodeEvent));
            closeables.add(this.rereadNodesTask);
        }
    }

    @Override
    protected void closeImpl() {
        Closeables.closeAll(this.closeables);
        this.scheduledExecutor.shutdownNow();
    }

    @Override
    protected void cleanImpl() {
        Map<String, SwarmNode> nodes = nodesMap.getOldValue();
        if(nodes == null) {
            nodes = nodesMap.get();
        }
        List<String> managers = new ArrayList<>();
        {
            DockerService manager = getDocker();
            nodes.forEach((s, swarmNode) -> {
                // first we remove all non manager nodes
                if(isManager(swarmNode)) {
                    managers.add(s);
                    return;
                }
                leave(manager, s, swarmNode);
            });
        }
        if(managers.isEmpty()) {
            // it mean wrong cluster state
            return;
        }
        // then remove all
        // here we must use only last manager
        int last = managers.size() - 1;
        String lastManagerName = managers.get(last);
        DockerService lastManager = getNodeStorage().getDockerService(lastManagerName);
        for(int i = 0; i < last; ++i) {
            String managerName = managers.get(i);
            SwarmNode swarmNode = nodes.get(managerName);
            leave(lastManager, managerName, swarmNode);
        }
        // the last manager, which is can not be removed safely
        SwarmLeaveArg arg = new SwarmLeaveArg();
        arg.setForce(true);
        lastManager.leaveSwarm(arg);
    }

    private void onNodeEvent(NodeEvent e) {
        final NodeEvent.Action action = e.getAction();
        NodeInfo old = e.getOld();
        NodeInfo curr = e.getCurrent();
        final String thisCluster = getName();
        boolean nowOur = curr != null && thisCluster.equals(curr.getCluster());
        boolean wasOur = old != null && thisCluster.equals(old.getCluster());
        final String nodeName = e.getNode().getName();
        if(action.isPre()) {
            // we cancel some events only for for master nodes
            if(!managers.containsKey(nodeName)) {
                return;
            }
            if(action == NodeEvent.Action.PRE_DELETE ||
                    action == NodeEvent.Action.PRE_UPDATE && wasOur && !nowOur) {
                e.cancel();
                log.warn("Cancel node '{}' deletion due to it is a manager.", nodeName);
            }
            return;
        }
        if(!wasOur && !nowOur || getStateCode() != S_INITED) {
            //if node not our we skip event, not that 'pre' event processed only when it affect 'manager' nodes
            return;
        }
        Map<String, SwarmNode> map = this.nodesMap.getOrNull();
        if(map == null) {
            map = this.nodesMap.getOldValue();
        }
        //note that map always will be null when cluster is invalid
        if (wasOur != nowOur || map == null || !map.containsKey(nodeName)) {
            scheduleRereadNodes();
        }
        this.createDefaultNetwork();
    }

    private void initCluster(String leaderName) {
        //first we must find if one of nodes has exists cluster
        Map<String, List<String>> clusters = new HashMap<>();
        Manager selectedManager = null;
        int onlineManagers = 0;
        for(Manager node: managers.values()) {
            DockerService service = node.getService();
            if(service == null) {
                continue;
            }
            SwarmInfo swarm;
            try {
                DockerServiceInfo info = service.getInfo();
                swarm = info.getSwarm();
            } catch (RuntimeException e) {
                log.error("Can not get swarm info from manager '{}' due to error:", node.name, e);
                // this case must not increment onlineManagers counter!
                continue;
            }
            onlineManagers++;
            if(swarm != null) {
                String clusterId = swarm.getClusterId();
                if(clusterId == null) {
                    continue;
                }
                clusters.computeIfAbsent(clusterId, (k) -> new ArrayList<>()).add(node.name);
                if(swarm.isManager()) {
                    selectedManager = node;
                }
            }
        }
        if(onlineManagers == 0) {
            cancelInit("because no online masters");
            return;
        }
        if(clusters.size() > 1) {
            throw new IllegalStateException("Managers nodes already united into different cluster: " + clusters);
        }
        if(clusters.isEmpty()) {
            //we must create cluster if no one found
            selectedManager = createCluster(leaderName);
        } else if(selectedManager == null) {
            throw new IllegalStateException("We has cluster: " + clusters + " but no one managers.");
        }

        // this.data.get() does not work while cluster is not inited
        ClusterData clusterData = loadClusterData(selectedManager.getService());
        if(clusterData == null) {
            // add managers later, because select manager is not ready yet
            return;
        }

        //and then we must join all managers to created cluster
        for(Manager node: managers.values()) {
            if(node == selectedManager) {
                continue;
            }
            try {
                joinAsManager(node, clusterData);
            } catch (Exception e) {
                log.error("Can not join additional manager '{}' to cluster {}, error:", node.name, getName(), e);
            }
        }
    }

    private Manager createCluster(String leader) {
        Manager manager = managers.get(leader);
        log.info("Begin initializing swarm-mode cluster on '{}'", manager.name);
        SwarmInitCmd cmd = new SwarmInitCmd();
        cmd.setSpec(getSwarmConfig());
        DockerService service = manager.getService();
        String address = getSwarmAddress(service);
        cmd.setListenAddr(address);
        SwarmInitResult res = service.initSwarm(cmd);
        if(res.getCode() != ResultCode.OK) {
            throw new IllegalStateException("Can not initialize swarm-mode cluster on '" + manager.name + "' due to error: " + res.getMessage());
        }
        log.info("Initialized swarm-mode cluster on '{}' at address {}", manager.name, address);
        return manager;
    }

    private SwarmSpec getSwarmConfig() {
        return new SwarmSpec();
    }

    private void scheduleRereadNodes() {
        rereadNodesTask.schedule();
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
            if (InetAddresses.forString(address).isLoopbackAddress()) {
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
        NodeStorage ns = getNodeStorage();
        return ns.updateNode(nodeName, Integer.MAX_VALUE, b -> {
           String nodeCluster = b.getCluster();
           final String cluster = getName();
           final String id = sn == null ? null : sn.getId();
           if (!cluster.equals(nodeCluster)) {
               // node remove
               if (Objects.equals(b.getIdInCluster(), id)) {
                   b.setVersion(0);
                   b.setIdInCluster(null);
                   b.setHealth(NodeMetrics.builder().from(b.getHealth()).manager(null).build());
               }
               return;
           }
           b.idInCluster(id);
           // we must update address, because cluster node may report wrong value
           // see getNodeAddress(SwarmNode)
            b.addressIfNeed(address);
            NodeMetrics.Builder nmb = NodeMetrics.builder();
            NodeMetrics.State state;
            if (sn != null) {
                state = getState(sn);
                b.version(sn.getVersion().getIndex());
                nmb.manager(isManager(sn));
                Map<String, String> labels = sn.getDescription().getEngine().getLabels();
                if(labels != null) {
                    b.labels(labels);
                }
            } else {
                state = NodeMetrics.State.ALONE;
            }
            setNodeState(nmb, state);
            b.mergeHealth(nmb.build());
        });
    }

    private void setNodeState(NodeMetrics.Builder nmb, NodeMetrics.State state) {
        nmb.state(state);
        nmb.healthy(state == NodeMetrics.State.HEALTHY);
    }

    private NodeMetrics.State getState(SwarmNode sn) {
        SwarmNode.NodeAvailability availability = sn.getSpec().getAvailability();
        SwarmNode.NodeState state = sn.getStatus().getState();
        if (state == SwarmNode.NodeState.READY && availability == SwarmNode.NodeAvailability.ACTIVE) {
            return NodeMetrics.State.HEALTHY;
        }
        if (state == SwarmNode.NodeState.DOWN ||
                availability == SwarmNode.NodeAvailability.DRAIN ||
                availability == SwarmNode.NodeAvailability.PAUSE) {
            return NodeMetrics.State.MAINTENANCE;
        }
        if (state == SwarmNode.NodeState.DISCONNECTED) {
            return NodeMetrics.State.DISCONNECTED;
        }
        return NodeMetrics.State.UNHEALTHY;

    }

    private boolean isFromSameCluster(NodeRegistration nr) {
        if (nr == null) {
            return false;
        }
        return isFromSameCluster(nr.getNodeInfo());
    }

    private boolean isFromSameCluster(NodeInfo ni) {
        return ni != null && (this.managers.containsKey(ni.getName()) ||
                getName().equals(ni.getCluster()));
    }

    private DockerService getDockerOrNull() {
        // sometime offline service is online, but it need test request
        // therefore we hold one of them for cases when no choice
        DockerService candidate = null;
        // we can not force load, because it may cause recursion when predefined managers is offline
        Map<String, SwarmNode> nodesMap = this.nodesMap.getOldValue();
        if(nodesMap != null) {
            for(Map.Entry<String, SwarmNode> e: nodesMap.entrySet()) {
                if(!isManager(e.getValue())) {
                    continue;
                }
                String node = e.getKey();
                NodeRegistration nr = getNodeStorage().getNodeRegistration(node);
                if(nr == null) {
                    continue;
                }
                DockerService service = nr.getDocker();
                if(service != null) {
                    if(service.isOnline()) {
                        return service;
                    } else {
                        candidate = service;
                    }
                }
            }
        }
        // when no other managers we try to use one of predefined
        for (Manager node : managers.values()) {
            DockerService service = node.getService();
            if (service != null) {
                if(service.isOnline()) {
                    return service;
                }
                candidate = service;
            }
        }
        return candidate;
    }

    private ClusterData loadClusterData(DockerService manager) {
        SwarmInspectResponse swarm = manager.getSwarm();
        DockerServiceInfo info = manager.getInfo();
        JoinTokens tokens = swarm.getJoinTokens();
        // at first cluster creation it may be null, due to creation may consume time
        SwarmInfo swarmInfo = info.getSwarm();
        if(swarmInfo == null) {
            return null;
        }
        return ClusterData.builder()
                .managerToken(tokens.getManager())
                .workerToken(tokens.getWorker())
                .managers(swarmInfo.getManagers())
                .build();
    }

    /**
     * Load SwarmNode managed by {@link #getDockerOrNull()} docker
     * @return
     */
    private Map<String, SwarmNode> loadNodesMap() {
        DockerService docker = getDockerOrNull();
        if(docker == null) {
            return null;
        }
        List<SwarmNode> nodes = docker.getNodes(null);
        // docker may produce node duplicated
        // see https://github.com/docker/docker/issues/24088
        // therefore we must fina one actual node in duplicates
        Map<String, SwarmNode> map = new HashMap<>();
        nodes.forEach(sn -> {
            String nodeName = getNodeName(sn);
            map.compute(nodeName, (key, old) -> {
                // use new node if old null or down
                if(old == null || old.getStatus().getState() == SwarmNode.NodeState.DOWN) {
                    old = sn;
                }
                return old;
            });
        });
        return map;
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
