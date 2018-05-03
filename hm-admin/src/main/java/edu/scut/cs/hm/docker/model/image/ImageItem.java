package edu.scut.cs.hm.docker.model.image;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class ImageItem {

    private final Date created;
    private final String id;
    private final List<String> repoTags;
    private final long size;
    private final long virtualSize;
    private final Map<String, String> labels;

    @Builder
    public ImageItem(@JsonProperty("Id") String id,
                     @JsonProperty("Created") long created,
                     @JsonProperty("RepoTags") List<String> repoTags,
                     @JsonProperty("Size") long size,
                     @JsonProperty("VirtualSize") long virtualSize,
                     @JsonProperty("Labels") Map<String, String> labels
    ) {
        this.id = id;
        // docker's date is in seconds
        this.created = new Date(created * 1000L);
        this.repoTags = repoTags == null? ImmutableList.of() : ImmutableList.copyOf(repoTags);
        this.size = size;
        this.virtualSize = virtualSize;
        this.labels = labels == null? ImmutableMap.of() : ImmutableMap.copyOf(labels);
    }
}
