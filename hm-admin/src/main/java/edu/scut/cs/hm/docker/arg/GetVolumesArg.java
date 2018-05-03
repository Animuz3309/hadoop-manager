package edu.scut.cs.hm.docker.arg;

import lombok.Data;

import java.util.Map;

/**
 * Planned for 'filters':
 * <pre>
 * JSON encoded value of the filters (a map[string][]string) to process on the volumes list. Available filters:
 name=<volume-name> Matches all or part of a volume name.
 dangling=<boolean> When set to true (or 1), returns all volumes that are not in use by a container.
 When set to false (or 0), only volumes that are in use by one or more containers are returned.
 driver=<volume-driver-name> Matches all or part of a volume driver name.
 * </pre>
 */
@Data
public class GetVolumesArg {
    private Map<String, String> filters;
}
