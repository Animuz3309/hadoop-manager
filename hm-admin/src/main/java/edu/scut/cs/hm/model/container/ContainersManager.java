package edu.scut.cs.hm.model.container;

import edu.scut.cs.hm.docker.arg.*;
import edu.scut.cs.hm.docker.model.container.ContainerDetails;
import edu.scut.cs.hm.docker.model.container.DockerContainer;
import edu.scut.cs.hm.docker.res.CreateAndStartContainerResult;
import edu.scut.cs.hm.docker.res.ServiceCallResult;

import java.util.Collection;

/**
 * Tool for manage cluster containers <p/>
 * Old standalone swarm can show cluster containers, but swarm-mode, use tasks & service concept.
 * TODO need to finish
 */
public interface ContainersManager {
    /**
     * Collection of swarm-mode services. Not all clusters support this, then return empty collection.
     * @return non null collection
     */
    Collection<ContainerService> getServices();

    /**
     * Collection of all containers (include service tasks and standalone containers)
     * @return non null collection
     */
    Collection<DockerContainer> getContainers();

    /**
     * Create container. Note that it created on
     * @param arg argument
     * @return container creation result
     */
    CreateAndStartContainerResult createContainer(CreateContainerArg arg);

    /**
     * Update (edit) container.
     * @param arg arg
     * @return update container result
     */
    ServiceCallResult updateContainer(EditContainerArg arg);
    ServiceCallResult stopContainer(StopContainerArg arg);
    ServiceCallResult restartContainer(StopContainerArg arg);
    ServiceCallResult startContainer(String containerId);
    ServiceCallResult pauseContainer(String containerId);
    ServiceCallResult unpauseContainer(String containerId);
    ServiceCallResult deleteContainer(DeleteContainerArg arg);
    ServiceCallResult scaleContainer(ScaleContainerArg arg);
    ContainerDetails getContainer(String id);

    ContainerService getService(String id);
    /**
     * Create service, when cluster does not supported services underline code emulate them.
     * @param arg argument
     * @return service creation result
     */
    ServiceCallResult createService(CreateServiceArg arg);

    /**
     * Update (edit) service.
     * @param arg arg
     * @return update service result
     */
    ServiceCallResult updateService(UpdateServiceArg arg);
    ServiceCallResult deleteService(String service);
}
