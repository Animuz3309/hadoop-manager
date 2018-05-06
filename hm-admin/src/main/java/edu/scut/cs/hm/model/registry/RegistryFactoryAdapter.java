package edu.scut.cs.hm.model.registry;

import edu.scut.cs.hm.admin.service.RegistryFactory;

public interface RegistryFactoryAdapter<T extends RegistryConfig> {
    RegistryService create(RegistryFactory factory, T config);

    /**
     * Fill some config values from other. Operation must be idempotent.
     * @param config
     */
    void complete(T config);
}
