package edu.scut.cs.hm.admin.config;

import com.google.common.collect.ImmutableList;
import edu.scut.cs.hm.admin.config.configurer.PropertyAclServiceConfigurer;
import edu.scut.cs.hm.admin.config.configurer.PropertyUserDetailsServiceConfigurer;
import edu.scut.cs.hm.admin.config.configurer.TokenServiceConfigurer;
import edu.scut.cs.hm.admin.web.filter.TokenAuthenticationFilterConfigurer;
import edu.scut.cs.hm.admin.web.filter.TokenHeaderRequestMatcher;
import edu.scut.cs.hm.admin.security.AccessContextFactory;
import edu.scut.cs.hm.admin.security.SecuredType;
import edu.scut.cs.hm.admin.security.TempTenantService;
import edu.scut.cs.hm.admin.security.acl.*;
import edu.scut.cs.hm.admin.security.authentication.UserCompositeAuthProvider;
import edu.scut.cs.hm.admin.security.userdetails.CompositeUserDetailsService;
import edu.scut.cs.hm.admin.security.userdetails.ConfigurableUserDetailService;
import edu.scut.cs.hm.common.security.*;
import edu.scut.cs.hm.common.security.acl.*;
import edu.scut.cs.hm.common.security.token.SignedTokenServiceImpl;
import edu.scut.cs.hm.common.security.token.TokenService;
import edu.scut.cs.hm.common.security.token.TokenValidator;
import edu.scut.cs.hm.common.security.token.TokenValidatorImpl;
import net.sf.ehcache.Cache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCache;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.acls.model.SidRetrievalStrategy;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.*;

/**
 * Security Configuration for Authentication and ACL
 */
@Configuration
public class SecurityConfiguration {

    // ========================================== Security for global ===============================================
    /**
     * Text Encryptor
     * @param password
     * @param salt
     * @return
     */
    @Bean
    public TextEncryptor textEncryptor(@Value("${hm.security.cipher.password}") String password,
                                @Value("${hm.security.cipher.salt}") String salt) {
        // on wrong configuration system will pass prop expressions '${prop}' as value, we need to detect this
        Assert.isTrue(StringUtils.hasText(password) && !password.startsWith("${"),
                "'hm.security.cipher.password' is invalid.");
        Assert.isTrue(StringUtils.hasText(salt) && !salt.startsWith("${"),
                "'hm.security.cipher.salt' is invalid.");
        return Encryptors.text(password, salt);
    }

    /**
     * User password encoder
     * @param bcryptStrength
     * @return
     */
    @Bean
    public PasswordEncoder getPasswordEncoder(@Value("${hm.bcrypt.strength:8}") int bcryptStrength) {
        return new BCryptPasswordEncoder(bcryptStrength);
    }

    /**
     * AccessDecisionManager with a customize vote {@link edu.scut.cs.hm.common.security.AdminRoleVoter}
     * so only tenant admin can pass
     * @return
     */
    @Bean
    public AccessDecisionManager accessDecisionManager() {
        ImmutableList.Builder<AccessDecisionVoter<?>> lb = ImmutableList.builder();
        lb.add(new AdminRoleVoter());
        return new AffirmativeBased(lb.build());
    }

    @Bean
    public TenantService tenantService() {
        return new TempTenantService();
    }

    /**
     * UserDetailsService
     */
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



    // ========================================== Security for acl ===============================================

    /**
     * 许可授予策略
     */
    @Bean
    public ExtPermissionGrantingStrategy extPermissionGrantingStrategy(TenantService tenantService, UserDetailsService userDetailsService) {
        PermissionGrantingJudgeDefaultBehavior behavior = new PermissionGrantingJudgeDefaultBehavior(tenantService);
        return new TenantBasedPermissionGrantedStrategy(behavior, userDetailsService, tenantService);
    }

    /**
     * 维护权限继承树
     */
    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.builder()
                .childs(Authorities.ADMIN_ROLE, Authorities.USER_ROLE)
                .build();
    }

    /**
     * 从authentication获取sid的策略
     */
    @Bean
    public SidRetrievalStrategy sidRetrievalStrategy(RoleHierarchy roleHierarchy) {
        return new TenantSidRetrievalStrategy(roleHierarchy);
    }

    /**
     * AccessContext factory to create AccessContext which keep the authentication information
     */
    @Bean
    public AccessContextFactory aclContextFactory(AclService aclService,
                                           ExtPermissionGrantingStrategy pgs,
                                           SidRetrievalStrategy sidStrategy) {
        return new AccessContextFactory(aclService, pgs, sidStrategy);
    }

    /**
     * AclService
     */
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
        public ConfigurableAclService configurableAclService(PermissionGrantingStrategy pgs) {
            ConfigurableAclService.Builder b = ConfigurableAclService.builder();
            for (AclServiceConfigurer configurer: configurers) {
                configurer.configure(b);
            }
            return b.permissionGrantingStrategy(pgs).build();
        }

        @Bean
        @Lazy
        public ProvidersAclService providersAclService(PermissionGrantingStrategy pgs) {
            ProvidersAclService service = new ProvidersAclService(pgs);
            if(providers != null) {
                service.getProviders().putAll(providers);
            }
            return service;
        }

        @Bean
        @Primary
        public AbstractAclService compositeAclService(List<AbstractAclService> services) {
            return new CompositeAclService(services);
        }
    }



    // ========================================== Security for webmvc ===============================================

    @Configuration
    @EnableConfigurationProperties(TokenServiceConfigurer.class)
    public static class TokenServiceConfiguration {

        private CacheManager cacheManager;

        @Autowired(required = false)
        public void setCacheManager(CacheManager cacheManager) {
            this.cacheManager = cacheManager;
        }

        @Bean
        public TokenService signedTokenService(TokenServiceConfigurer configurer) {
            SignedTokenServiceImpl tokenService = new SignedTokenServiceImpl();
            tokenService.setPseudoRandomNumberBytes(configurer.getPseudoRandomNumberBytes());
            tokenService.setServerSecret(configurer.getServerSecret());
            tokenService.setServerInteger(configurer.getServerInteger());
            tokenService.setSecureRandom(new SecureRandom());
            tokenService.setDigestAlgorithm(configurer.getDigestAlgorithm());
            return tokenService;
        }

        @Bean
        public TokenValidator tokenValidator(TokenService tokenService,
                                      TokenServiceConfigurer configurer) {
            // net.sf.ehcache.Cache
            Cache cache = new Cache(
                    new CacheConfiguration("tokenServiceCache", 1000)
                            .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
                            .eternal(false)
                            .timeToLiveSeconds(configurer.getExpireAfterInSec() * 2)
                            .timeToIdleSeconds(configurer.getExpireLastAccessInSec() * 2)
            );

            Objects.requireNonNull(((EhCacheCacheManager) cacheManager).getCacheManager()).addCache(cache);
            return TokenValidatorImpl.builder()
                    .cache(new EhCacheCache(cache))
                    .expireAfterInSec(configurer.getExpireAfterInSec())
                    .expireLastAccessInSec(configurer.getExpireLastAccessInSec())
                    .tokenService(tokenService)
                    .build();
        }
    }

    /**
     * Success auth processor after AuthenticationProvider
     * @return
     */
    @Bean
    public SuccessAuthProcessor successAuthProcessor() {
        return (auth, userDetails) -> {
            Set<GrantedAuthority> authorities = new HashSet<>(userDetails.getAuthorities());
            authorities.add(Authorities.USER);

            // we add GA for username, because we do not implement ACL tuning for this,
            // and anyway check ngroup and node access
            authorities.add(Authorities.fromName(SecuredType.LOCAL_IMAGE.admin()));
            authorities.add(Authorities.fromName(SecuredType.REMOTE_IMAGE.admin()));
            authorities.add(Authorities.fromName(SecuredType.NETWORK.admin()));

            final UsernamePasswordAuthenticationToken result =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,                // principal
                            userDetails.getPassword(),  // credentials
                            authorities);               // GA
            result.setDetails(auth.getDetails());
            return result;
        };
    }

    @Bean
    public AuthenticationProvider userCompositedAuthProvider(UserIdentifiersDetailsService userDetailsService,
                                                      PasswordEncoder passwordEncoder,
                                                      SuccessAuthProcessor successAuthProcessor) {
        return new UserCompositeAuthProvider(userDetailsService, passwordEncoder, successAuthProcessor);
    }
}
