package edu.scut.cs.hm.docker.model.image;

import edu.scut.cs.hm.docker.model.container.ContainerConfig;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

/**
 * Common iface for image descriptor
 */
public interface ImageDescriptor {
    /**
     * Id of image. Usually start with hash name prefix.
     * @return
     */
    String getId();
    Date getCreated();

    /**
     * map of image labels, usually it retrieved from container config
     * @return
     */
    default Map<String, String> getLabels() {
        ContainerConfig cc = getContainerConfig();
        Map<String, String> labels = cc == null? null : cc.getLabels();
        return labels == null? Collections.emptyMap() : Collections.unmodifiableMap(labels);
    }

    /**
     * Container config. Implementation may lazy load this data, and therefore it may consume some time.
     * @return
     */
    ContainerConfig getContainerConfig();
}

