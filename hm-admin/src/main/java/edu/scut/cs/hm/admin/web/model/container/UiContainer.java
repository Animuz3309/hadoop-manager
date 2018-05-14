package edu.scut.cs.hm.admin.web.model.container;

import edu.scut.cs.hm.admin.service.NodeStorage;
import edu.scut.cs.hm.admin.web.model.UiPermission;
import edu.scut.cs.hm.admin.web.model.UiUtils;
import edu.scut.cs.hm.docker.DockerService;
import edu.scut.cs.hm.docker.model.container.DockerContainer;
import edu.scut.cs.hm.docker.model.network.Port;
import edu.scut.cs.hm.model.ContainerBaseIface;
import edu.scut.cs.hm.model.WithUiPermission;
import edu.scut.cs.hm.model.cluster.DiscoveryStorage;
import edu.scut.cs.hm.model.container.ContainerRegistration;
import edu.scut.cs.hm.model.container.ContainerStorage;
import edu.scut.cs.hm.model.ngroup.NodesGroup;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;

import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * UI representation for Container
 */
@Data
public class UiContainer implements Comparable<UiContainer>, UiContainerIface, WithUiPermission {
    /**
     * It mean that node is offline.
     */
    public static final String NO_NODE = "Node is offline";
    @NotNull
    protected String id;
    @NotNull protected String name;
    @NotNull protected String node;
    @NotNull protected String image;
    @NotNull protected String imageId;
    protected String application;
    protected String cluster;
    protected final List<String> command = new ArrayList<>();
    protected final List<Port> ports = new ArrayList<>();
    protected String status;
    protected DockerContainer.State state;
    protected Date created;
    protected boolean lock;
    protected String lockCause;
    protected final Map<String, String> labels = new HashMap<>();
    protected boolean run;
    private UiPermission permission;

    @Override
    public int compareTo(UiContainer o) {
        int comp = ObjectUtils.compare(cluster, o.cluster);
        if(comp == 0) {
            comp = ObjectUtils.compare(application, o.application);
        }
        if(comp == 0) {
            comp = ObjectUtils.compare(node, o.node);
        }
        if(comp == 0) {
            comp = ObjectUtils.compare(name, o.name);
        }
        if(comp == 0) {
            comp = ObjectUtils.compare(image, o.image);
        }
        if(comp == 0) {
            comp = ObjectUtils.compare(id, o.id);
        }
        return comp;
    }

    public static UiContainer from(DockerContainer container) {
        UiContainer uic = new UiContainer();
        return from(uic, container);
    }

    public static  UiContainer from(UiContainer uic, DockerContainer container) {
        fromBase(uic, container);
        uic.setNode(container.getNode());
        uic.setCreated(new Date(container.getCreated()));
        uic.getPorts().addAll(container.getPorts());
        String status = container.getStatus();
        uic.setStatus(status);
        uic.setState(container.getState());
        uic.setRun(container.isRun());
        // this is workaround, because docker use simply command representation in container,
        // for full you need use ContainerDetails
        String command = container.getCommand();
        if(command != null) {
            uic.getCommand().add(command);
        }
        return uic;
    }

    public static UiContainer fromBase(UiContainer uic, ContainerBaseIface container) {
        uic.setId(container.getId());
        uic.setName(container.getName());
        uic.setImage(container.getImage());
        uic.setImageId(container.getImageId());
        uic.getLabels().putAll(container.getLabels());
        UiUtils.resolveContainerLock(uic, container);
        return uic;
    }

    /**
     * Fill container data with some values from specified storages.
     * @param discoveryStorage
     * @param containerStorage
     */
    public void enrich(DiscoveryStorage discoveryStorage, ContainerStorage containerStorage) {
        //note that cluster can be virtual
        String node = getNode();
        if(node != null) {
            NodesGroup nodeCluster = discoveryStorage.getClusterForNode(node);
            if(nodeCluster != null) {
                setCluster(nodeCluster.getName());
            }
        }

        ContainerRegistration registration = containerStorage.getContainer(getId());
        if (registration != null && registration.getAdditionalLabels() != null) {
            getLabels().putAll(registration.getAdditionalLabels());
        }
    }

    /**
     * Override status when node is offline. Require filled {@link #getNode()} value.
     * @param uc container
     * @param nodeStorage storage
     */
    public static void resolveStatus(UiContainer uc, NodeStorage nodeStorage) {
        String node = uc.getNode();
        DockerService ds = node == null? null : nodeStorage.getDockerService(node);
        if(ds == null || !ds.isOnline()) {
            uc.setRun(false);
            uc.setState(null);// state is unknown
            uc.setStatus(UiContainer.NO_NODE);
        }
    }
}
