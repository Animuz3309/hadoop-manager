package edu.scut.cs.hm.admin.web.model.container;

import edu.scut.cs.hm.admin.component.ContainerSourceFactory;
import edu.scut.cs.hm.admin.web.model.UiPermission;
import edu.scut.cs.hm.admin.web.model.UiUtils;
import edu.scut.cs.hm.docker.model.container.*;
import edu.scut.cs.hm.model.WithUiPermission;
import edu.scut.cs.hm.model.source.ContainerSource;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * Container details. Must strong complement with {@link ContainerSource } for filling UI forms with default
 * values and etc.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UiContainerDetails extends ContainerSource implements UiContainerIface, WithUiPermission {

    private Date created;
    private Date started;
    private Date finished;
    @Deprecated
    private List<String> args;
    private Integer restartCount;
    private boolean lock;
    private String lockCause;
    private boolean run;
    private String status;
    private DockerContainer.State state;
    private UiPermission permission;

    public UiContainerDetails() {
    }

    public UiContainerDetails from(ContainerSourceFactory containerSourceFactory, ContainerDetails container) {
        containerSourceFactory.toSource(container, this);
        setId(container.getId());
        UiUtils.resolveContainerLock(this, container);
        setArgs(container.getArgs());
        setRestartCount(container.getRestartCount());
        ContainerState state = container.getState();
        setStatus(state.getStatus());
        setRun(state.isRunning());
        setCreated(container.getCreated());
        setStarted(state.getStartedAt());
        setFinished(state.getFinishedAt());
        HostConfig hostConfig = container.getHostConfig();
        if (hostConfig != null) {
            RestartPolicy restartPolicy = hostConfig.getRestartPolicy();
            setRestart(restartPolicy != null ? restartPolicy.toString() : null);
            setPublishAllPorts(Boolean.TRUE.equals(hostConfig.getPublishAllPorts()));
        }
        return this;
    }
}
