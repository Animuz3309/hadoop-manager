package edu.scut.cs.hm.model.container;

import edu.scut.cs.hm.docker.model.image.ImageDescriptor;

/**
 * Provide {@link ContainerSource}
 */
public interface ConfigProvider {
    ContainerSource resolveProperties(String cluster, ImageDescriptor image, String imageName,
                                      ContainerSource createContainerArg);
}
