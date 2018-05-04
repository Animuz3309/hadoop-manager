package edu.scut.cs.hm.model.container;

import edu.scut.cs.hm.admin.component.ContainerCreator;
import edu.scut.cs.hm.docker.DockerService;
import edu.scut.cs.hm.docker.arg.*;
import edu.scut.cs.hm.docker.cmd.UpdateContainerCmd;
import edu.scut.cs.hm.docker.model.container.ContainerDetails;
import edu.scut.cs.hm.docker.model.container.DockerContainer;
import edu.scut.cs.hm.docker.model.container.EditableContainerSource;
import edu.scut.cs.hm.docker.res.CreateAndStartContainerResult;
import edu.scut.cs.hm.docker.res.ServiceCallResult;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

/**
 * A virtual swarm container manager tool
 * @see edu.scut.cs.hm.model.cluster.DefaultNodesGroupImpl
 */
public class SwarmClusterContainers extends AbstractContainersManager {

    private final ContainerCreator containerCreator;

    public SwarmClusterContainers(Supplier<DockerService> supplier, ContainerCreator containerCreator) {
        super(supplier);
        Assert.notNull(supplier, "supplier can't be null");
        this.containerCreator = containerCreator;
    }

    @Override
    public Collection<ContainerService> getServices() {
        return Collections.emptyList();
    }

    @Override
    public Collection<DockerContainer> getContainers() {
        return getDocker().getContainers(new GetContainersArg(true));
    }

    @Override
    public CreateAndStartContainerResult createContainer(CreateContainerArg arg) {
        return this.containerCreator.createContainer(arg, getDocker());
    }

    @Override
    public ServiceCallResult updateContainer(EditContainerArg arg) {
        UpdateContainerCmd cmd = new UpdateContainerCmd();
        EditableContainerSource src = arg.getSource();
        cmd.from(src);
        cmd.setId(arg.getContainerId());
        return getDocker().updateContainer(cmd);
    }

    @Override
    public ServiceCallResult stopContainer(StopContainerArg arg) {
        return getDocker().stopContainer(arg);
    }

    @Override
    public ServiceCallResult startContainer(String containerId) {
        return getDocker().startContainer(containerId);
    }

    @Override
    public ServiceCallResult pauseContainer(String containerId) {
        return getDocker().pauseContainer(containerId);
    }

    @Override
    public ServiceCallResult unpauseContainer(String containerId) {
        return getDocker().unpauseContainer(containerId);
    }

    @Override
    public ServiceCallResult deleteContainer(DeleteContainerArg arg) {
        return getDocker().deleteContainer(arg);
    }

    @Override
    public ServiceCallResult restartContainer(StopContainerArg arg) {
        return getDocker().restartContainer(arg);
    }

    @Override
    public ServiceCallResult scaleContainer(ScaleContainerArg arg) {
        return containerCreator.scale(getDocker(), arg.getScale(), arg.getContainerId());
    }

    @Override
    public ContainerDetails getContainer(String id) {
        return getDocker().getContainer(id);
    }

    @Override
    public ContainerService getService(String id) {
        return null;
    }

    @Override
    public ServiceCallResult createService(CreateServiceArg arg) {
        return ServiceCallResult.unsupported();
    }

    @Override
    public ServiceCallResult updateService(UpdateServiceArg arg) {
        return ServiceCallResult.unsupported();
    }

    @Override
    public ServiceCallResult deleteService(String service) {
        return ServiceCallResult.unsupported();
    }
}

