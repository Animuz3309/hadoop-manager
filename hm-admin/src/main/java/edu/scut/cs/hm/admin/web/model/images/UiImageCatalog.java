package edu.scut.cs.hm.admin.web.model.images;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.reverseOrder;

@Data
public class UiImageCatalog implements Comparable<UiImageCatalog> {
    private final String name;
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private final String registry;
    @JsonIgnore
    @lombok.Getter(value = AccessLevel.NONE)
    private final Map<String, UiImageData> ids = new TreeMap<>();
    private final Set<String> clusters = new TreeSet<>();

    public UiImageCatalog(String name, String registry) {
        this.name = name;
        this.registry = registry;
    }

    @JsonProperty
    public List<UiImageData> getIds() {
        return ids.values().stream().sorted(reverseOrder(UiImageData::compareTo)).collect(Collectors.toList());
    }

    public UiImageData getOrAddId(String id) {
        return ids.computeIfAbsent(id, UiImageData::new);
    }

    @Override
    public int compareTo(UiImageCatalog o) {
        return name.compareTo(o.name);
    }
}
