package edu.scut.cs.hm.admin.config;

import edu.scut.cs.hm.admin.filter.AccessContextFilter;
import edu.scut.cs.hm.admin.filter.TokenAuthenticationFilterConfigurer;
import edu.scut.cs.hm.admin.filter.TokenHeaderRequestMatcher;
import edu.scut.cs.hm.admin.security.AccessContextFactory;
import edu.scut.cs.hm.admin.security.authentication.TokenAuthProvider;
import edu.scut.cs.hm.common.security.SecurityUtils;
import edu.scut.cs.hm.common.security.SuccessAuthProcessor;
import edu.scut.cs.hm.common.security.token.TokenValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;
    private final AuthenticationProvider provider;
    private final TokenAuthenticationFilterConfigurer<HttpSecurity> configurer;
    private final AccessContextFactory aclContextFactory;


    @Autowired
    public WebSecurityConfiguration(TokenValidator tokenValidator,
                                    UserDetailsService userDetailsService,
                                    SuccessAuthProcessor authProcessor,
                                    AuthenticationProvider provider,
                                    AccessContextFactory aclContextFactory) {
        this.userDetailsService = userDetailsService;
        this.provider = provider;
        this.configurer = new TokenAuthenticationFilterConfigurer<>(
                new TokenHeaderRequestMatcher(),
                new TokenAuthProvider(tokenValidator, this.userDetailsService, authProcessor));
        this.aclContextFactory = aclContextFactory;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        final String loginUrl = "/login";
        final String logoutUrl = "/logout";
        http.csrf().disable()
                .authenticationProvider(provider).userDetailsService(userDetailsService)
                .anonymous().principal(SecurityUtils.USER_ANONYMOUS).and()
                .authorizeRequests().antMatchers("/token/login").permitAll()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers("/**").authenticated()
                .and().headers().cacheControl().disable()
                .and().formLogin().loginPage(loginUrl).permitAll().defaultSuccessUrl("/hello")
                .and().logout().logoutUrl(logoutUrl).logoutSuccessUrl(loginUrl)
                .and().rememberMe().key("uniqueAndSecret").userDetailsService(userDetailsService)
                .and().apply(configurer);

        http.headers()
                .frameOptions().sameOrigin();

        // acl service
        http.addFilterAfter(new AccessContextFilter(aclContextFactory), SwitchUserFilter.class);

        http.httpBasic();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        // be care for not /static/**
        web.ignoring().antMatchers(
                "/webjars/**",
                "/js/**",
                "/css/**",
                "/font/**",
                "/img/**");
    }
}
