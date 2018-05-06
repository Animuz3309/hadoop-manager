package edu.scut.cs.hm.docker.model.image;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Information about tags.
 */
public class Tags {

    private static final String NAME = "name"; // Name of the target repository.
    private static final String TAGS = "tags"; // A list of tags for the named repository.

    private final String name;
    private final List<String> tags;

    public Tags(@JsonProperty(NAME) String name,
                @JsonProperty(TAGS) List<String> tags) {
        this.name = name;
        this.tags = tags;
    }

    @JsonProperty(NAME)
    public String getName() {
        return name;
    }

    @JsonProperty(TAGS)
    public List<String> getTags() {
        return tags;
    }

    @Override
    public String toString() {
        return "Tags{" +
                "name='" + name + '\'' +
                ", tags=" + tags +
                '}';
    }
}
