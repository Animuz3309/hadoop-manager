package edu.scut.cs.hm.model.registry.core;

import edu.scut.cs.hm.admin.service.RegistryFactory;
import edu.scut.cs.hm.model.registry.RegistryConfig;

public interface RegistryFactoryAdapter<T extends RegistryConfig> {
    RegistryService create(RegistryFactory factory, T config);

    /**
     * Fill some config values from other. Operation must be idempotent.
     * @param config
     */
    void complete(T config);
}
