package edu.scut.cs.hm.admin.security;

import edu.scut.cs.hm.admin.config.SecurityConfiguration;
import edu.scut.cs.hm.common.security.AuthenticationImpl;
import edu.scut.cs.hm.common.security.Authorities;
import edu.scut.cs.hm.common.security.GrantedAuthorityImpl;
import edu.scut.cs.hm.common.security.MultiTenancySupport;
import edu.scut.cs.hm.common.security.acl.dto.Action;
import edu.scut.cs.hm.common.security.acl.dto.ObjectIdentityData;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@Slf4j
@ActiveProfiles("test_acl_service")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AccessContextTest.AppConfiguration.class)
public class AccessContextTest {

    public static class ServiceControlledByAcl {
        final AccessContextFactory acf;

        ObjectIdentityData cluster1 = SecuredType.CLUSTER.id("cluster1");
        ObjectIdentityData container1 = SecuredType.CONTAINER.id("container1");

        ServiceControlledByAcl(AccessContextFactory acf) {
            this.acf = acf;
        }

        void checkAccess(AccessContext context,ObjectIdentityData id, Action action) {
            boolean granted = context.isGranted(id, action);
            if (!granted) {
                throw new AccessDeniedException("Access to cluster '" + cluster1 + "' with action " + action + " is denied");
            }
        }

        void create() {
            AccessContext ac = acf.getContext();
            checkAccess(ac, cluster1, Action.CREATE);
        }

        void read() {
            AccessContext ac = acf.getContext();
            checkAccess(ac, container1, Action.READ);
        }
    }

    @Configuration
    @EnableAutoConfiguration
    @Import(SecurityConfiguration.class)
    public static class AppConfiguration {
    }

    @Autowired
    private AccessContextFactory acf;
    @Autowired
    private UserDetailsService userDetailsService;

    private ServiceControlledByAcl service;
    private UserDetails user;

    @Before
    public void before() {
        service = new ServiceControlledByAcl(acf);
        user = userDetailsService.loadUserByUsername("username");
    }

    @Test
    public void testRoles() {
        // 没有经过认证 -> 没有 Authentication
        try {
            create();
            fail("create must be failed");
        } catch (AccessDeniedException e) {
            log.debug("Expect fail: {}", e.getMessage());
            assertEquals("No credentials in context.", e.getMessage());
        }

        // 拥有 Authentication，但权限为0
        try(TempAuth auth = TempAuth.open(createUserAuth())) {
            create();
            fail("create must be failed");
        } catch (AccessDeniedException e) {
            log.debug("Expect fail: {}", e.getMessage());
            assertTrue(e.getMessage().contains("denied"));
        }

        // 拥有Cluster的admin权限
        try(TempAuth auth = TempAuth.open(createUserAuth(Authorities.USER_ROLE, SecuredType.CLUSTER.admin()))) {
            create();
        }

        try(TempAuth auth = TempAuth.asSystem()) {
            create();
        }
    }

    @Test
    public void testAcl() {
        // @see application-test_acl_service.properties
        // username@root has authority ROLE_USER@root
        // [CONTAINER@container1] grant ROLE_USER@root RUA
        try(TempAuth auth = TempAuth.open(createAuthFromDetails(user))) {
            read();
        }
    }

    private Authentication createAuthFromDetails(UserDetails user) {
        return AuthenticationImpl.builder()
                .authorities(user.getAuthorities())
                .authenticated(true)
                .principal(user)
                .name(user.getUsername())
                .build();
    }

    private Authentication createUserAuth(String... roles) {
        List<GrantedAuthorityImpl> authorities = Arrays.stream(roles)
                .map((r) -> new GrantedAuthorityImpl(r, MultiTenancySupport.ROOT_TENANT))
                .collect(Collectors.toList());
        return AuthenticationImpl.builder()
                .principal(user)
                .authorities(authorities).build();
    }

    private void create() {
        service.create();
    }

    private void read() {
        service.read();
    }
}
