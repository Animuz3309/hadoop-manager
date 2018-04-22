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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Encrypt.JUnit4JasyptConfiguration.class)
public class Encrypt {

    @Configuration
    @Import(JasyptConfiguration.class)
    static class JUnit4JasyptConfiguration {

    }

    @Autowired
    private StringEncryptor encryptor;

    @Value("${msg:$2a$08$bFLBfYL8Eb6n71D/yvLyLu9QzxDWEPG0TTx3/LgfiwaKdhfyCEdVe}")
    private String msg;

    @Test
    public void encrypt() {
        String encode = encryptor.encrypt(msg);
        String decode = encryptor.decrypt(encode);
        log.info(encode);
        log.info(decode);
        assertEquals(msg, decode);
    }

}
