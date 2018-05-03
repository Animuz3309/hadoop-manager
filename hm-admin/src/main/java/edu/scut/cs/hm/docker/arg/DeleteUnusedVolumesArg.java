package edu.scut.cs.hm.docker.arg;

import lombok.Data;

import java.util.Map;

/**
 * Planned for 'filters':
 * <pre>
 *     nothing filters are documented
 * </pre>
 */
@Data
public class DeleteUnusedVolumesArg {
    private Map<String, String> filters;
}
