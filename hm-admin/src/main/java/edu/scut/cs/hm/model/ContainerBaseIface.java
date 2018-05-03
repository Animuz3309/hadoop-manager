package edu.scut.cs.hm.model;

import edu.scut.cs.hm.docker.model.swarm.SwarmUtils;
import edu.scut.cs.hm.model.Labels;
import edu.scut.cs.hm.model.Named;

/**
 * Basic iface for container
 */
public interface ContainerBaseIface extends Named, Labels {

    String getId();

    String getImage();

    String getImageId();

    /**
     * Id of service, which own this container, or null. <p/>
     * @see SwarmUtils#LABEL_SERVICE_ID
     * @return service id or null
     */
    default String getService() {
        return getLabels().get(SwarmUtils.LABEL_SERVICE_ID);
    }
}
