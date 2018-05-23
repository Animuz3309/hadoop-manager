package edu.scut.cs.hm.model.registry;

import edu.scut.cs.hm.docker.model.image.ImageDescriptor;
import edu.scut.cs.hm.model.Labels;
import edu.scut.cs.hm.model.Named;
import edu.scut.cs.hm.model.registry.core.RegistryService;

import java.util.Map;

/**
 * Context for filtering one image
 */
public class ImageFilterContext implements Named, Labels {
    private final RegistryService registryService;
    private String name;
    private String tag;
    private ImageDescriptor image;

    public ImageFilterContext(RegistryService registryService) {
        this.registryService = registryService;
    }

    public void setName(String name) {
        this.name = name;
        reset();
    }


    public void setTag(String tag) {
        this.tag = tag;
        reset();
    }

    /**
     * Current registry service
     * @return
     */
    public RegistryService getRegistryService() {
        return registryService;
    }

    private void reset() {
        this.image = null;
    }

    /**
     * Retrieve labels. <b>This operation is time consuming.</b>
     * @return
     */
    public Map<String, String> getLabels() {
        return getImage().getContainerConfig().getLabels();
    }


    /**
     * Retrieve image description from service. <b>This operation is time consuming.</b>
     * @return
     */
    public ImageDescriptor getImage() {
        if(this.image == null) {
            this.image = registryService.getImage(name, tag);
        }
        return image;
    }

    /**
     * Name of current image
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Tag of current image
     * @return
     */
    public String getTag() {
        return tag;
    }
}
