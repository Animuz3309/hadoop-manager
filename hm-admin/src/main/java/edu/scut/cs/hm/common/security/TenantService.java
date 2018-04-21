package edu.scut.cs.hm.common.security;

import java.util.Collection;
import java.util.List;

public abstract class TenantService<T extends Tenant> {
    /**
     * Judge tenant is Root
     * @param tenant
     * @return
     */
    public abstract boolean isRoot(String tenant);

    /**
     * Collecting child tenants of specified tenant
     * @param tenant
     * @param childTenants  target collection in which will be added child tenants
     */
    public void getChildTenants(String tenant, Collection<String> childTenants) {
        if (MultiTenancySupport.isNoTenant(tenant)) {
            return;
        }

        final T tenantObj = getTenant(tenant);
        if (tenantObj == null) {
            return;
        }
        load(tenantObj, childTenants);
    }

    private void load(T tenant, Collection<String> childTenants) {
        List<T> childs = getChilds(tenant);
        if(childs == null) {
            return;
        }
        for(T child: childs) {
            childTenants.add(child.getName());
            load(child, childTenants);
        }
    }

    /**
     * Get specified tenant object from string tenant (tenant name)
     * @param tenant
     * @return
     */
    protected abstract T getTenant(String tenant);

    /**
     * Get List of tenant's children
     * @param tenant
     * @return
     */
    protected abstract List<T> getChilds(T tenant);
}
