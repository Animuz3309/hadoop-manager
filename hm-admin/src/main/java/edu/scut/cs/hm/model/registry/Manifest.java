package edu.scut.cs.hm.model.registry;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

/**
 * Image manifest
 */
@Value
@AllArgsConstructor(onConstructor = @__(@JsonCreator))
public class Manifest {

    @Value
    @AllArgsConstructor(onConstructor = @__(@JsonCreator))
    public static class Entry {
        private final String mediaType;
        private final long size;
        private final String digest;
    }

    private final int schemaVersion;
    private final String mediaType;
    private final Entry config;
    private final List<Entry> layers;
}
