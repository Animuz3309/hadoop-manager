package edu.scut.cs.hm.common.security.token;

import org.apache.commons.codec.binary.Base32;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Token service based on digest algorithm
 */
public class SignedTokenServiceImpl implements TokenService {
    static final String TYPE = "sit";
    private final Base32 base32 = new Base32();
    private int pseudoRandomNumberBytes = 32;
    private String serverSecret;
    private Integer serverInteger;
    private SecureRandom secureRandom;
    private String digestAlgorithm = "sha1";

    static String pack(String... strs) {
        StringBuilder sb = new StringBuilder();
        for (String str: strs) {
            if (sb.length() != 0) {
                sb.append(',');
            }
            sb.append(str);
        }
        return sb.toString();
    }

    static String[] unpack(String str) {
        return StringUtils.split(str, ",");
    }

    @Override
    public TokenData createToken(TokenConfiguration config) {
        final long creationTime = System.currentTimeMillis();

        byte[] serverSecret = computeServerSecretApplicableAt(creationTime);
        byte[] content = contentPack(creationTime,
                generateRandom(),
                serverSecret,
                Utf8.encode(pack(config.getUsername(), config.getDeviceHash())));
        byte[] sign = sign(content);

        ByteBuffer buffer = ByteBuffer.allocate(1 + sign.length +
                                                1 + content.length);
        store(buffer, sign);
        store(buffer, content);

        String key = base32.encodeAsString(buffer.array());

        return new TokenDataImpl(
                config.getUsername(),
                config.getDeviceHash(),
                TokenUtils.getKeyWithTypeAndToken(TYPE, key),
                creationTime);
    }

    private byte[] sign(byte[] s) {
        try {
            MessageDigest ins = MessageDigest.getInstance(digestAlgorithm);
            return ins.digest(s);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Invalid algorithm: " + digestAlgorithm, e);
        }
    }

    private byte[] generateRandom() {
        byte[] randomBytes = new byte[pseudoRandomNumberBytes];
        secureRandom.nextBytes(randomBytes);
        return randomBytes;
    }

    private byte[] contentPack(long creationTime, byte[] random, byte[] serverSecret, byte[] payload) {
        ByteBuffer buffer = ByteBuffer.allocate(8 +
                1 + random.length +
                1 + serverSecret.length +
                1 + payload.length);
        buffer.putLong(creationTime);
        store(buffer, random);
        store(buffer, serverSecret);
        store(buffer, payload);
        return buffer.array();
    }

    private void store(ByteBuffer bf, byte[] arr) {
        bf.put((byte) arr.length);
        bf.put(arr);
    }

    private byte[] computeServerSecretApplicableAt(long time) {
        return Utf8.encode(serverSecret + ":" + (time % serverInteger));
    }

    @Override
    public TokenData getToken(String token) {
        final String key = unwrapToken(token);
        if (key.isEmpty()) {
            return null;
        }

        byte[] decodedKey = base32.decode(key);
        byte[] actualSign;
        final long creationTime;
        byte[] random;
        byte[] payload;

        {
            // unwrap {actualSign content}
            byte[] content;
            {
                ByteBuffer buffer = ByteBuffer.wrap(decodedKey);
                actualSign = restore(buffer);
                content = restore(buffer);
            }

            // unwrap content {creationTime, random, serverSecret, payload}
            ByteBuffer contentBuffer = ByteBuffer.wrap(content);
            creationTime = contentBuffer.getLong();
            random = restore(contentBuffer);
            // skip serverSecret
            restore(contentBuffer);
            payload = restore(contentBuffer);
        }

        final byte[] serverSecret = computeServerSecretApplicableAt(creationTime);
        // Verification
        byte[] expectedSign = sign(contentPack(creationTime, random, serverSecret, payload));
        Assert.isTrue(Arrays.equals(expectedSign, actualSign), "Key verification failure");

        String[] unpack = unpack(Utf8.decode(payload));
        return new TokenDataImpl(unpack[0], unpack[1], TokenUtils.getKeyWithTypeAndToken(TYPE, key), creationTime);
    }

    private byte[] restore(ByteBuffer bf) {
        int len = ((int) bf.get() & 0xff);
        byte[] bytes = new byte[len];
        bf.get(bytes);
        return bytes;
    }

    private String unwrapToken(String token) {
        if(token == null || token.isEmpty()) {
            throw new TokenException("Token is null or empty");
        }
        if(!token.startsWith(TYPE)) {
            throw new TokenException("Token '" + token + "' is not supported by this backend.");
        }
        return token.substring(TYPE.length() + 1);
    }

    @Override
    public void removeToken(String token) {
        throw new UnsupportedOperationException("This operation is unsupported for this implementation.");
    }

    @Override
    public void removeUserTokens(String username) {
        throw new UnsupportedOperationException("This operation is unsupported for this implementation.");
    }

    /**
     * @param serverSecret the new secret, which can contain a ":" if desired (never being sent to the client)
     */
    public void setServerSecret(String serverSecret) {
        this.serverSecret = serverSecret;
    }

    public void setSecureRandom(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }

    /**
     * @param pseudoRandomNumberBytes changes the number of bytes issued (must be >= 0; defaults to 256)
     */
    public void setPseudoRandomNumberBytes(int pseudoRandomNumberBytes) {
        Assert.isTrue(pseudoRandomNumberBytes >= 0, "Must have a positive pseudo random number bit size");
        this.pseudoRandomNumberBytes = pseudoRandomNumberBytes;
    }

    public void setServerInteger(Integer serverInteger) {
        this.serverInteger = serverInteger;
    }

    /**
     * Digest algorithm like 'SHA-256'. <p/>
     * See <a href="http://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#MessageDigest">javadoc</a>
     * @return
     */
    public String getDigestAlgorithm() {
        return digestAlgorithm;
    }

    /**
     * * Digest algorithm like 'SHA-256'. <p/>
     * See <a href="http://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#MessageDigest">javadoc</a>
     * @param digestAlgorithm
     */
    public void setDigestAlgorithm(String digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
    }
}
