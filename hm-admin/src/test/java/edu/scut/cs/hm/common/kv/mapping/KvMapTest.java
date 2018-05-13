package edu.scut.cs.hm.common.kv.mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;
import edu.scut.cs.hm.admin.config.KvConfiguration;
import edu.scut.cs.hm.common.kv.KeyValueStorage;
import edu.scut.cs.hm.common.utils.UUIDs;
import lombok.Data;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.validation.Validator;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = KvMapTest.AppConfig.class)
@TestPropertySource(properties = {
        "hm.kv.etcd-urls = http://localhost:2370",
        "hm.kv.prefix    = /test"
})
public class KvMapTest {

    @Configuration
    @EnableAutoConfiguration
    @Import(KvConfiguration.class)
    public static class AppConfig {
    }

    @Autowired
    private KvMapperFactory factory;

    @Data
    public static class Entry {
        private static AtomicInteger counter = new AtomicInteger();
        @KvMapping
        private String text;
        @KvMapping
        private int number;

        public Entry() {
            this.text = UUIDs.longUid();
            this.number = counter.incrementAndGet();
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("hash", Integer.toUnsignedString(System.identityHashCode(this), 32))
                    .add("text", text)
                    .add("number", number)
                    .toString();
        }
    }

    @Test
    public void test() throws Exception {
        KvMap<Entry> map = KvMap.builder(Entry.class)
                .mapper(factory)
                .path(factory.getStorage().getPrefix() + "/entries")
                .build();

        // init map is empty
        assertThat(map.list(), hasSize(0));

        // put 'one' and 'two' entry and test has the value
        final Entry one = new Entry();
        final String oneKey = "one";
        final Entry two = new Entry();
        final String twoKey = "two";
        map.put(oneKey, one);
        map.put(twoKey, two);
        assertThat(map.list(), contains(oneKey, twoKey));

        // test if update value
        Entry newOne = new Entry();
        map.put(oneKey, newOne);
        {
            Entry oneActual = map.get(oneKey);
            assertEquals(newOne, oneActual);
            assertSame(newOne, oneActual);

            Entry twoActual = map.get(twoKey);
            assertEquals(two, twoActual);
            assertSame(two, twoActual);
        }
    }


}