package edu.scut.cs.hm.admin.security;

import edu.scut.cs.hm.admin.config.CachingConfiguration;
import edu.scut.cs.hm.admin.config.JasyptConfiguration;
import edu.scut.cs.hm.admin.config.SecurityConfiguration;
import edu.scut.cs.hm.common.security.ExtendedUserDetailsImpl;
import edu.scut.cs.hm.common.security.GrantedAuthorityImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;


@Slf4j
@ActiveProfiles({"test_userdetails", "dev"})
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = UserDetailsServiceTest.AppConfiguration.class)
public class UserDetailsServiceTest {

    @Configuration
    @Import({CachingConfiguration.class, JasyptConfiguration.class, SecurityConfiguration.class})
    @EnableAutoConfiguration
    public static class AppConfiguration {
    }

    @Autowired
    private UserDetailsService userDetailsService;
    private UserDetails admin;
    private UserDetails user;
    private UserDetails otherUser;

    @Before
    public void before() {
        admin = userDetailsService.loadUserByUsername("admin");
        user = userDetailsService.loadUserByUsername("username");
        otherUser = userDetailsService.loadUserByUsername("otherUser");
    }

    @Test
    public void testUserDetailsService() {
        assertEquals("password", admin.getPassword());
        ExtendedUserDetailsImpl userExpected = ExtendedUserDetailsImpl.builder()
                .username("username")
                .password("password")
                .tenant("root")
                .title("Sample username")
                .email(null)
                .authorities(Arrays.asList(
                        new GrantedAuthorityImpl("ROLE_CONTAINER_USER", "root"),
                        new GrantedAuthorityImpl("ROLE_USER", "root")
                )).build();
        ExtendedUserDetailsImpl otherUserExpected = ExtendedUserDetailsImpl.builder()
                .username("otherUser")
                .tenant("root")
                .authorities(Arrays.asList(
                        new GrantedAuthorityImpl("ROLE_OWNEDCLUSTER_USER", "root"),
                        new GrantedAuthorityImpl("ROLE_USER", "root")
                )).build();
        assertEquals(userExpected, user);
        assertEquals(otherUserExpected, otherUser);
    }
}
