package edu.scut.cs.hm.admin.security;

import edu.scut.cs.hm.admin.config.CachingConfiguration;
import edu.scut.cs.hm.admin.config.SecurityConfiguration;
import edu.scut.cs.hm.admin.config.WebSecurityConfiguration;
import edu.scut.cs.hm.common.security.token.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

@Slf4j
@ActiveProfiles("dev")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TokenServiceTest.AppConfiguration.class)
public class TokenServiceTest {

    @Configuration
    @EnableAutoConfiguration
    @Import({CachingConfiguration.class, SecurityConfiguration.class})
    static class AppConfiguration {
    }

    @Autowired
    private TokenValidator tokenValidator;
    @Autowired
    private TokenService tokenService;

    @Test
    public void test() {
        TokenData expectedToken = tokenService.createToken(createConfig("user1", "hash1"));
        TokenData actualToken = null;
        try {
            actualToken = tokenValidator.verifyToken(expectedToken.getKey(), "hash1");
        } catch (TokenException e) {
            log.debug("Expect fail: {}", e.getMessage());
        }

        try {
            actualToken = tokenValidator.verifyToken(expectedToken.getKey(), "hash1");
        } catch (TokenException e) {
            log.debug("Expect fail: {}", e.getMessage());
        }
        assertEquals(expectedToken, actualToken);

    }

    public TokenConfiguration createConfig(String username, String deviceHash) {
        TokenConfiguration config = new TokenConfiguration();
        config.setUsername(username);
        config.setDeviceHash(deviceHash);
        return config;
    }
}
