package edu.scut.cs.hm.admin.config;

import com.google.common.collect.ImmutableList;
import edu.scut.cs.hm.admin.config.configurer.PropertyAclServiceConfigurer;
import edu.scut.cs.hm.admin.config.configurer.PropertyUserDetailsServiceConfigurer;
import edu.scut.cs.hm.admin.security.AccessContextFactory;
import edu.scut.cs.hm.admin.security.TempTenantService;
import edu.scut.cs.hm.admin.security.acl.*;
import edu.scut.cs.hm.admin.security.userdetails.CompositeUserDetailsService;
import edu.scut.cs.hm.admin.security.userdetails.ConfigurableUserDetailService;
import edu.scut.cs.hm.common.security.*;
import edu.scut.cs.hm.common.security.acl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.acls.model.SidRetrievalStrategy;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Map;

/**
 * Security Configuration for Authentication and ACL
 */
@Configuration

public class SecurityConfiguration {

    /**
     * AccessDecisionManager with a customize vote {@link edu.scut.cs.hm.common.security.AdminRoleVoter}
     * so only tenant admin can pass
     * @return
     */
    @Bean
    AccessDecisionManager accessDecisionManager() {
        ImmutableList.Builder<AccessDecisionVoter<?>> lb = ImmutableList.builder();
        lb.add(new AdminRoleVoter());
        return new AffirmativeBased(lb.build());
    }

    @Bean
    TenantService tenantService() {
        return new TempTenantService();
    }

    /**
     * 许可授予策略
     */
    @Bean
    ExtPermissionGrantingStrategy extPermissionGrantingStrategy(TenantService tenantService, UserDetailsService userDetailsService) {
        PermissionGrantingJudgeDefaultBehavior behavior = new PermissionGrantingJudgeDefaultBehavior(tenantService);
        return new TenantBasedPermissionGrantedStrategy(behavior, userDetailsService, tenantService);
    }

    /**
     * 维护权限继承树
     */
    @Bean
    RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.builder()
                .childs(Authorities.ADMIN_ROLE, Authorities.USER_ROLE)
                .build();
    }

    /**
     * 从authentication获取sid的策略
     */
    @Bean
    SidRetrievalStrategy sidRetrievalStrategy(RoleHierarchy roleHierarchy) {
        return new TenantSidRetrievalStrategy(roleHierarchy);
    }

    /**
     * AccessContext factory to create AccessContext which keep the authentication information
     */
    @Bean
    AccessContextFactory aclContextFactory(AclService aclService,
                                           ExtPermissionGrantingStrategy pgs,
                                           SidRetrievalStrategy sidStrategy) {
        return new AccessContextFactory(aclService, pgs, sidStrategy);
    }

    @Configuration
    @EnableConfigurationProperties(PropertyUserDetailsServiceConfigurer.class)
    public static class UserDetailsServiceConfiguration {

        @Bean
        @Order
        UserIdentifiersDetailsService configurableUserDetailsService(PropertyUserDetailsServiceConfigurer configurer) {
            return new ConfigurableUserDetailService(configurer);
        }

        @Bean
        @Primary
        UserIdentifiersDetailsService compositeUserDetailsService(List<UserIdentifiersDetailsService> services) {
            return new CompositeUserDetailsService(services);
        }
    }

    @EnableConfigurationProperties(PropertyAclServiceConfigurer.class)
    @Configuration
    public static class AclServiceConfiguration {

        private List<AclServiceConfigurer> configurers;
        private Map<String, AclProvider> providers;

        @Autowired(required = false)
        public void setConfigurers(List<AclServiceConfigurer> configurers) {
            this.configurers = configurers;
        }

        @Autowired(required = false)
        public void setProviders(Map<String, AclProvider> providers) {
            this.providers = providers;
        }

        @Bean
        @Order
        ConfigurableAclService configurableAclService(PermissionGrantingStrategy pgs) {
            ConfigurableAclService.Builder b = ConfigurableAclService.builder();
            for (AclServiceConfigurer configurer: configurers) {
                configurer.configure(b);
            }
            return b.permissionGrantingStrategy(pgs).build();
        }

        @Bean
        @Order(Ordered.HIGHEST_PRECEDENCE)
        ProvidersAclService providersAclService(PermissionGrantingStrategy pgs) {
            ProvidersAclService service = new ProvidersAclService(pgs);
            if(providers != null) {
                service.getProviders().putAll(providers);
            }
            return service;
        }

        @Bean
        @Primary
        AclService compositeAclService(List<AbstractAclService> services) {
            return new CompositeAclService(services);
        }
    }
}
