package edu.scut.cs.hm.admin.config;

import edu.scut.cs.hm.admin.config.configurer.TokenServiceConfigurer;
import edu.scut.cs.hm.common.security.token.SignedTokenServiceImpl;
import edu.scut.cs.hm.common.security.token.TokenService;
import edu.scut.cs.hm.common.security.token.TokenValidatorImpl;
import edu.scut.cs.hm.common.security.token.TokenValidtor;
import net.sf.ehcache.Cache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCache;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import java.security.SecureRandom;
import java.util.Objects;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {


    @Configuration
    @EnableConfigurationProperties({
            TokenServiceConfigurer.TokenValidtorConfigurer.class,
            TokenServiceConfigurer.SignedTokenServiceConfigurer.class})
    public static class TokenServiceConfiguration {

        private CacheManager cacheManager;

        @Autowired(required = false)
        public void setCacheManager(CacheManager cacheManager) {
            this.cacheManager = cacheManager;
        }

        @Bean
        TokenService signedTokenService(TokenServiceConfigurer.SignedTokenServiceConfigurer configurer) {
            SignedTokenServiceImpl tokenService = new SignedTokenServiceImpl();
            tokenService.setPseudoRandomNumberBytes(configurer.getPseudoRandomNumberBytes());
            tokenService.setServerSecret(configurer.getServerSecret());
            tokenService.setServerInteger(configurer.getServerInteger());
            tokenService.setSecureRandom(new SecureRandom());
            tokenService.setDigestAlgorithm(configurer.getDigestAlgorithm());
            return tokenService;
        }

        @Bean
        TokenValidtor tokenValidtor(TokenService tokenService,
                                    TokenServiceConfigurer.TokenValidtorConfigurer configurer) {
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
}
