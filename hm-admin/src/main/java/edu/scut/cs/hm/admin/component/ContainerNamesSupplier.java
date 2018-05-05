package edu.scut.cs.hm.admin.component;

import edu.scut.cs.hm.docker.DockerService;
import edu.scut.cs.hm.docker.arg.GetContainersArg;
import edu.scut.cs.hm.docker.model.container.DockerContainer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 */
@Component
public class ContainerNamesSupplier implements Function<DockerService, Collection<String>> {

    @Override
    public Collection<String> apply(DockerService dockerService) {
        List<DockerContainer> containerIfaces = getContainers(dockerService);
        List<String> names = new ArrayList<>(containerIfaces.size());
        for(DockerContainer containerIface : containerIfaces) {
            String name = containerIface.getName();
            names.add(name);
        }
        return names;
    }

    private List<DockerContainer> getContainers(DockerService dockerService) {
        return dockerService.getContainers(new GetContainersArg(true));
    }

}
