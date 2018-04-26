package edu.scut.cs.hm.kv.etcd;

import edu.scut.cs.hm.admin.config.EtcdConfiguration;
import edu.scut.cs.hm.kv.KeyValueStorage;
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
import static org.junit.Assert.assertNull;

@ActiveProfiles("dev")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = EtcdClientWrapperTest.Config.class)
public class EtcdClientWrapperTest {

    @Configuration
    @EnableAutoConfiguration
    @Import(EtcdConfiguration.class)
    public static class Config {
    }

    @Autowired
    private KeyValueStorage etcdClientWrapper;

    @Test
    public void test() throws Exception {
        final String key = "key";
        final String val = "Val";
        etcdClientWrapper.set(key, val);
        String res = etcdClientWrapper.get(key).getValue();
        assertEquals(val, res);
        etcdClientWrapper.delete(key, null);
        assertNull(etcdClientWrapper.get(key));
    }
}