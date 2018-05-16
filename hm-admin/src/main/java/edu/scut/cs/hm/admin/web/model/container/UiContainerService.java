package edu.scut.cs.hm.admin.web.model.container;

import edu.scut.cs.hm.admin.web.model.UiPermission;
import edu.scut.cs.hm.docker.model.swarm.Service;
import edu.scut.cs.hm.docker.model.swarm.Task;
import edu.scut.cs.hm.model.WithUiPermission;
import edu.scut.cs.hm.model.container.ContainerService;
import edu.scut.cs.hm.model.ngroup.NodesGroup;
import edu.scut.cs.hm.model.node.NodeInfo;
import edu.scut.cs.hm.model.source.ContainerSource;
import edu.scut.cs.hm.model.source.ServiceSource;
import edu.scut.cs.hm.model.source.ServiceSourceConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.ObjectUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * UI representation for Container service.
 * @see ContainerService
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UiContainerService extends ServiceSource implements WithUiPermission {
    protected long version;
    protected LocalDateTime created;
    protected LocalDateTime updated;
    private UiPermission permission;
    private long runningReplicas;
    private final List<UiServiceTask> tasks = new ArrayList<>();

    @Override
    public int compareTo(ServiceSource o) {
        int comp = ObjectUtils.compare(getCluster(), o.getCluster());
        if(comp == 0) {
            comp = ObjectUtils.compare(getApplication(), o.getApplication());
        }
        if(comp == 0) {
            comp = ObjectUtils.compare(getName(), o.getName());
        }
        ContainerSource cs = getContainer();
        ContainerSource ocs = o.getContainer();
        if(comp == 0 && cs != null && ocs != null) {
            comp = ObjectUtils.compare(cs.getImage(), ocs.getImage());
        }
        if(comp == 0) {
            comp = ObjectUtils.compare(getId(), o.getId());
        }
        return comp;
    }

    /**
     * Convert service to its ui presentation
     * @param ng group which contains specified service
     * @param s service
     * @return ui presentation of service
     */
    public static UiContainerService from(NodesGroup ng, ContainerService s) {
        UiContainerService uic = new UiContainerService();
        Service srv = s.getService();
        uic.setId(srv.getId());
        Service.ServiceSpec srvSpec = srv.getSpec();
        uic.setVersion(srv.getVersion().getIndex());
        uic.setCreated(srv.getCreated());
        uic.setUpdated(srv.getUpdated());
        ServiceSourceConverter ssc = new ServiceSourceConverter();
        ssc.setNodesGroup(ng);
        ssc.setServiceSpec(srvSpec);
        ssc.toSource(uic);
        uic.setCluster(s.getCluster());
        List<UiServiceTask> tasks = uic.getTasks();
        Map<String, String> clusterIdToNodeName = ng.getNodes().stream().collect(Collectors.toMap(NodeInfo::getIdInCluster, NodeInfo::getName));
        s.getTasks().forEach(st -> {
            UiServiceTask ut = UiServiceTask.from(st, clusterIdToNodeName::get);
            if(ut.getState() == ut.getDesiredState() && ut.getDesiredState() == Task.TaskState.RUNNING) {
                //we may change it in future, therefore calc count of replicas on backend
                uic.runningReplicas++;
            }
            tasks.add(ut);
        });
        return uic;
    }

    @Data
    public static class UiServiceTask {

        private String id;
        private String container;
        private String node;
        private String error;
        private String message;
        private LocalDateTime timestamp;
        private Task.TaskState state;
        private Task.TaskState desiredState;

        public static UiServiceTask from(Task st, Function<String, String> nodeNameById) {
            UiServiceTask ut = new UiServiceTask();
            ut.setId(st.getId());
            ut.setNode(nodeNameById.apply(st.getNodeId()));
            Task.TaskStatus status = st.getStatus();
            ut.setError(status.getError());
            ut.setMessage(status.getMessage());
            ut.setTimestamp(status.getTimestamp());
            ut.setState(status.getState());
            ut.setDesiredState(st.getDesiredState());
            Task.ContainerStatus containerStatus = status.getContainerStatus();
            ut.setContainer(containerStatus.getContainerId());
            return ut;
        }
    }
}
