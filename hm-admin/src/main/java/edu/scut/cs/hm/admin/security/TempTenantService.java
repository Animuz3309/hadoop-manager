package edu.scut.cs.hm.admin.security;

import edu.scut.cs.hm.common.security.MultiTenancySupport;
import edu.scut.cs.hm.common.security.Tenant;
import edu.scut.cs.hm.common.security.TenantService;

import java.util.Collections;
import java.util.List;

/**
 * Temporary stub implementation for tenant service.
 */
public class TempTenantService extends TenantService<Tenant> {
    @Override
    public boolean isRoot(String tenant) {
        return MultiTenancySupport.ROOT_TENANT.equals(tenant);
    }

    @Override
    protected Tenant getTenant(String tenant) {
        return () -> tenant;
    }

    @Override
    protected List<Tenant> getChilds(Tenant tenant) {
        return Collections.emptyList();
    }
}
