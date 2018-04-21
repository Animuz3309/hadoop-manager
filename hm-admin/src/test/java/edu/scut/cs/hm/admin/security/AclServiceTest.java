package edu.scut.cs.hm.admin.security;

import edu.scut.cs.hm.admin.config.SecurityConfiguration;
import edu.scut.cs.hm.common.security.acl.TenantGrantedAuthoritySid;
import edu.scut.cs.hm.common.security.acl.TenantPrincipalSid;
import edu.scut.cs.hm.common.security.acl.dto.Action;
import edu.scut.cs.hm.common.security.acl.dto.PermissionData;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.AclService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

@Slf4j
@ActiveProfiles("test_acl")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AclServiceTest.AppConfiguration.class)
public class AclServiceTest {

    @Configuration
    @EnableAutoConfiguration(exclude = WebMvcAutoConfiguration.class)
    @Import(SecurityConfiguration.class)
    public static class AppConfiguration {
    }

    @Autowired
    private AclService aclService;

    @Test
    public void testConfigurableAclService() {
        assertNotNull("AbstractAclService not wired", aclService);
        assertNotNull("No acl for container type", aclService.readAclById(SecuredType.CONTAINER.typeId()));

        // type = CONTAINER id = syscont so 'CONTAINER@syscont'
        Acl syscont = aclService.readAclById(SecuredType.CONTAINER.id("syscont"));

        // acl = 'user@root, grant ROLE_USER@root CRUDE, revoke ROLE_USER@root D'
        assertNotNull("No acl for syscont container", syscont);

        // owner = 'user@root'
        assertEquals(new TenantPrincipalSid("user", "root"), syscont.getOwner());

        // aces = 'grant ROLE_USER@root CRUDE, revoke ROLE_USER@root D'
        List<AccessControlEntry> aces = syscont.getEntries();
        assertThat(aces, hasSize(2));

        // ace = 'revoke ROLE_USER@root D'
        AccessControlEntry ace = aces.get(1);
        // 'revoke' so ace.isGranting() = false
        assertFalse(ace.isGranting());
        // ace.getPermission() = 'D'
        assertEquals(PermissionData.from(Action.DELETE), ace.getPermission());
        // ace.getSid = 'ROLE_USER@root'
        assertEquals(new TenantGrantedAuthoritySid("ROLE_USER", "root"), ace.getSid());

    }
}
