package edu.scut.cs.hm.admin.security.acl;

/**
 * Iface to configure {@link ConfigurableAclService}
 */
public interface AclServiceConfigurer {
    void configure(ConfigurableAclService.Builder builder);
}
