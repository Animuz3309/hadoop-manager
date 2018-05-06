package edu.scut.cs.hm.docker.model.image;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 *
 * List of images available in the registry.
 */
public class ImageCatalog {

    private static final String REPOSITORIES = "repositories"; // images available in the registry

    private final List<String> images;
    private String name;

    public ImageCatalog(@JsonProperty(REPOSITORIES) List<String> repositories) {
        this.images = repositories;
    }

    @JsonProperty(REPOSITORIES)
    public List<String> getImages() {
        return images;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ImageCatalog{" +
                "images=" + images +
                '}';
    }
}
