package edu.scut.cs.hm.model.registry;

/**
 * Docker registry read-only API
 */
public interface DockerHubRegistry extends RegistryService {

    /**
     * Name of default docker hub registry. If we change it from '', then we need add some workarounds for
     * correct handling this case.
     */
    String DEFAULT_NAME = "";
}
