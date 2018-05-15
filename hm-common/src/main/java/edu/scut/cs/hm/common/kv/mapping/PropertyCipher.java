package edu.scut.cs.hm.common.kv.mapping;

import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.util.StringUtils;

/**
 * encrypt the string value of property
 */
public class PropertyCipher implements PropertyInterceptor {

    private final TextEncryptor encryptor;

    public PropertyCipher(TextEncryptor encryptor) {
        this.encryptor = encryptor;
    }

    @Override
    public String save(KvPropertyContext prop, String value) {
        if (StringUtils.hasText(value)) {
            return encryptor.encrypt(value);
        } else {
            return value;
        }
    }

    @Override
    public String read(KvPropertyContext prop, String value) {
        if (StringUtils.hasText(value)) {
            return encryptor.decrypt(value);
        } else {
            return value;
        }
    }
}