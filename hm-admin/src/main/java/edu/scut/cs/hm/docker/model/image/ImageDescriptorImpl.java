package edu.scut.cs.hm.docker.model.image;

import com.google.common.collect.ImmutableMap;
import edu.scut.cs.hm.docker.model.container.ContainerConfig;
import lombok.Data;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

@Data
public class ImageDescriptorImpl implements ImageDescriptor {

    private final String id;
    private final Date created;
    private final ContainerConfig containerConfig;
    private final Map<String, String> labels;

    @lombok.Builder(builderClassName = "Builder")
    private ImageDescriptorImpl(String id, Date created, ContainerConfig containerConfig, Map<String, String> labels) {
        this.id = id;
        this.created = created;
        this.containerConfig = containerConfig;
        this.labels = labels == null? Collections.emptyMap() : ImmutableMap.copyOf(labels);
    }
}