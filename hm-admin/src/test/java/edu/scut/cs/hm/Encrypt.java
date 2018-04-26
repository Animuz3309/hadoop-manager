package edu.scut.cs.hm;

import edu.scut.cs.hm.admin.config.JasyptConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

/**
 * You can use this for encode and encrypt text in your *.properties
 * <a href="https://github.com/ulisesbocchio/jasypt-spring-boot"></a>
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Encrypt.JUnit4JasyptConfiguration.class)
public class Encrypt {

    @Configuration
    @Import({JasyptConfiguration.class})
    static class JUnit4JasyptConfiguration {

    }

    @Autowired
    private StringEncryptor encryptor;

    @Autowired(required = false)
    private PasswordEncoder passwordEncoder;

    @Value("http://222.201.145.145:2370")
    private String text;

    @Test
    public void encrypt() {
        String encryptText = encryptor.encrypt(text);
        String actualText = encryptor.decrypt(encryptText);

        assertEquals(text, actualText);
        log.info(encryptText);
    }

    @Test
    public void encodeAndEncrypt() {

    }

}
