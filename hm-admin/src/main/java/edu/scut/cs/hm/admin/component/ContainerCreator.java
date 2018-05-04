package edu.scut.cs.hm.admin.component;

import edu.scut.cs.hm.common.utils.Consumers;
import edu.scut.cs.hm.docker.DockerService;
import edu.scut.cs.hm.docker.arg.CreateContainerArg;
import edu.scut.cs.hm.docker.res.CreateAndStartContainerResult;
import edu.scut.cs.hm.docker.res.ProcessEvent;
import edu.scut.cs.hm.docker.res.ServiceCallResult;
import edu.scut.cs.hm.model.node.NodeRegistry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.function.Consumer;

import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * Manager for containers, it use DockerService as backend, but do something things
 * which is not provided by docker out of box.
 * todo to finish it
 */
@Slf4j
@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Data
public class ContainerCreator {

    private final NodeRegistry nodeRegistry;

    /**
     * Create container by image information (image name, tag) also can be specified optional params <p/>
     * <b>Must not throw any exception after start creation of container.</b>
     * @param arg argument
     * @param docker cluster or node service
     * @return id of new container
     */
    public CreateAndStartContainerResult createContainer(CreateContainerArg arg, DockerService docker) {
        CreateContainerContext cc = new CreateContainerContext(arg, docker);
        return createContainerInternal(cc);
    }

    // todo
    private CreateAndStartContainerResult createContainerInternal(CreateContainerContext cc) {
        return null;
    }

    /**
     * add scalable to doc
     * @param docker swarm service
     * @param scaleFactor
     * @param id
     * @return resul
     */
    public ServiceCallResult scale(DockerService docker, Integer scaleFactor, String id) {
        return null;
    }

    private class CreateContainerContext {
        final CreateContainerArg arg;
        final Consumer<ProcessEvent> watcher;
        /**
         * Service instance of concrete node or cluster on which do creation .
         */
        final DockerService dockerService;
        private String name;

        CreateContainerContext(CreateContainerArg arg, DockerService service) {
            this.arg = arg;
            Assert.notNull(arg.getContainer(), "arg.container is null");
            this.watcher = firstNonNull(arg.getWatcher(), Consumers.<ProcessEvent>nop());
            if (service != null) {
                this.dockerService = service;
            } else {
                this.dockerService = getDocker(arg);
            }
        }

        private DockerService getDocker(CreateContainerArg arg) {
            //we create container only on node, ignore swarm and virtual service
            String node = arg.getContainer().getNode();
            Assert.hasText(node, "Node is null or empty");
            DockerService service = nodeRegistry.getDockerService(node);
            Assert.notNull(service, "Can not fins service for node: " + node);
            return service;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
