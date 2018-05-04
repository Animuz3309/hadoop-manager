package edu.scut.cs.hm.model.cluster;

import edu.scut.cs.hm.admin.component.ContainerCreator;
import edu.scut.cs.hm.admin.service.DiscoveryStorageImpl;
import edu.scut.cs.hm.admin.service.DockerServices;
import edu.scut.cs.hm.common.kv.WriteOptions;
import edu.scut.cs.hm.common.kv.mapping.KvMapperFactory;
import edu.scut.cs.hm.docker.DockerConfig;
import edu.scut.cs.hm.docker.DockerService;
import edu.scut.cs.hm.docker.DockerServiceInfo;
import edu.scut.cs.hm.docker.arg.NodeUpdateArg;
import edu.scut.cs.hm.docker.res.ServiceCallResult;
import edu.scut.cs.hm.model.container.ContainersManager;
import edu.scut.cs.hm.model.container.SwarmClusterContainers;
import edu.scut.cs.hm.model.ngroup.AbstractNodesGroup;
import edu.scut.cs.hm.model.ngroup.SwarmNodesGroupConfig;
import edu.scut.cs.hm.model.node.*;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * A kind of nodegroup which is managed by 'swarm'
 */
@Slf4j
@ToString(callSuper = true)
public class SwarmCluster extends AbstractNodesGroup<SwarmNodesGroupConfig> {


    /**
     * Judge the node from this cluster
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
        return getNodesInfo();
    }

    /**
     * Not support
     *
     * @param arg
     * @return
     */
    @Override
    public ServiceCallResult updateNode(NodeUpdateArg arg) {
        return ServiceCallResult.unsupported();
    }

    /**
     * empty collection, because all nodes belong to one cluster not other nodes group
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
        return docker;
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

    @Override
    protected void onConfig() {
        DockerConfig old = this.config.getConfig();
        DockerConfig fixed = fixConfig(old);
        this.config.setConfig(fixed);
    }

    public DockerConfig getDockerConfig() {
        synchronized (lock) {
            return this.config.getConfig();
        }
    }

    public void setDockerConfig(DockerConfig cc) {
        synchronized (lock) {
            DockerConfig newConf = fixConfig(cc);
            this.config.setConfig(newConf);
        }
    }

    /**
     * Sometime external config mey be partially filled, or has wrong value. We can not reject this,
     * for legacy reasons, therefore need to fix it manually.
     * @param cc source
     * @return fixed copy of config
     */
    private DockerConfig fixConfig(DockerConfig cc) {
        DockerConfig newConf;
        if(getName().equals(cc.getCluster())) {
            newConf = cc;
        } else {
            newConf = DockerConfig.builder(cc).cluster(getName()).build();
        }
        return newConf;
    }

    private KvMapperFactory kvmf;
    private DockerService docker;
    private ContainersManager containers;
    private ContainerCreator containerCreator;

    SwarmCluster(DiscoveryStorageImpl storage, SwarmNodesGroupConfig config) {
        super(config, storage, Collections.singleton(Feature.SWARM));
    }

    /**
     * Get docker service from {@link DockerServices}
     */
    @Override
    protected void initImpl() {
        getNodeStorage().getNodeEventSubscriptions().subscribe(this::onNodeEvent);
        this.containers = new SwarmClusterContainers(this::getDocker, this.containerCreator);

        DockerServices dses = this.getDiscoveryStorage().getDockerServices();
        this.docker = dses.getOrCreateDocker(getDockerConfig(), (dsb) -> dsb.setInfoInterceptor(this::dockerInfoModifier));
    }

    // modify docker service info
    private void dockerInfoModifier(DockerServiceInfo.Builder dib) {
        // swarm use name of host which run cluster, we must use cluster name for prevent user confusing
        dib.setName(getName());
        Map<String, NodeInfo> map = new HashMap<>();
        int offNodes = 0;
        // check the node in the swarm cluster
        for (NodeInfo nodeInfo : dib.getNodeList()) {
            map.put(nodeInfo.getName(), nodeInfo);
            if (!nodeInfo.isOn()) {
                offNodes++;
            }
        }

        for (NodeInfo nodeInfo : getNodesInfo()) {
            if (!map.containsKey(nodeInfo.getName())) {
                // if node is on but not inside cluster yet -> set status pending
                if (nodeInfo.isOn()) {
                    nodeInfo = NodeInfoImpl.builder()
                            .from(nodeInfo)
                            .health(NodeMetrics.builder()
                                    .from(nodeInfo.getHealth())
                                    .state(NodeMetrics.State.PENDING).build()).build();
                }
                map.put(nodeInfo.getName(), nodeInfo);
                offNodes++;
            }
        }
        dib.setNodeList(map.values());
        dib.getNodeList().sort(null);
        dib.setOffNodeCount(offNodes);
        dib.setNodeCount(dib.getNodeList().size() - offNodes);
    }

    @Autowired
    void setKvmf(KvMapperFactory kvmf) {
        this.kvmf = kvmf;
    }

    @Autowired
    void setContainerCreator(ContainerCreator containerCreator) {
        this.containerCreator = containerCreator;
    }

    private void onNodeEvent(NodeEvent event) {
        NodeEvent.Action action = event.getAction();
        if(NodeEvent.Action.OFFLINE == action || action.isPre()) {
            return;
        }
        NodeInfo ni = event.getCurrent();
        boolean delete = false;
        if(ni == null) {
            delete = true;
            ni = event.getOld();
        }
        String nodeName = ni.getName();
        String cluster = ni.getCluster();
        if (!StringUtils.hasText(cluster) || !getName().equals(cluster)) {
            return;
        }
        Assert.doesNotContain(cluster, "/", "Bad cluster name: " + cluster);
        String address = ni.getAddress();
        if(delete) {
            try {
                kvmf.getStorage().delete(getDiscoveryKey(cluster, address), null);
            } catch (Exception e) {
                log.error("Can not delete swarm registration: of node {} from cluster {}", address, cluster, e);
            }
            return;
        }
        NodeRegistration nr = getNodeStorage().getNodeRegistration(nodeName);
        int ttl = nr.getTtl();
        if (ttl < 1) {
            return;
        }
        // we update node record for swarm discovery in KV for each event
        try {
            kvmf.getStorage().set(getDiscoveryKey(cluster, address),
                    address,
                    WriteOptions.builder().ttl(ttl).build());
        } catch (Exception e) {
            log.error("Can not update swarm registration: of node {} from cluster {}", address, cluster, e);
        }
        createDefaultNetwork();
    }

    private String getDiscoveryKey(String cluster, String address) {
        return "/discovery/" + cluster + "/docker/swarm/nodes/" + address;
    }

    private List<NodeInfo> getNodesInfo() {
        return getNodeStorage().getNodes(this::isFromSameCluster);
    }

    private boolean isFromSameCluster(NodeRegistration nr) {
        return nr != null && getName().equals(nr.getNodeInfo().getCluster());
    }
}
