package edu.scut.cs.hm.model.container;

import java.util.List;

/**
 * Container service, a tool to get container from k-v storage
 * TODO need to finish
 */
public interface ContainerService {
    List<ContainerRegistration> getContainers();
    ContainerRegistration getContainer(String id);
}
