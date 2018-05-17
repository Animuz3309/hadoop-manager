package edu.scut.cs.hm.common.fc;

import com.google.common.io.BaseEncoding;
import org.springframework.util.Assert;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

final class FbUtils {
    private static final byte[] SIGN = new byte[]{(byte) 0xF1, (byte) 0x1E, (byte) 0xBA};
    static final int SIGN_LEN = SIGN.length;
    static final int MAX_STR_LEN = 256;
    private static final BaseEncoding HEX = BaseEncoding.base16();

    static void writeSign(DataOutput dao) throws IOException {
        dao.write(SIGN);
    }

    static void writeSign(ByteBuffer bb) {
        bb.put(SIGN);
    }

    static void readSign(DataInput dai) throws IOException {
        byte[] buf = new byte[SIGN_LEN];
        dai.readFully(buf);
        validateSign(buf);
    }

    static void readSign(ByteBuffer bb) {
        byte[] buf = new byte[SIGN.length];
        bb.get(buf);
        validateSign(buf);
    }

    private static void validateSign(byte[] buf) {
        if (!Arrays.equals(buf, SIGN)) {
            throw new FbException("Invalid file signature. Expected '" + HEX.encode(SIGN)+ "', but give: '" + HEX.encode(buf) + "'");
        }
    }

    static void readAndValidate(DataInput dai, byte expected) throws IOException {
        byte readed = dai.readByte();
        if(readed != expected) {
            throw new FbException(String.format("Expected %X byte, but give: %X", expected, readed));
        }
    }

    static void readAndValidate(ByteBuffer bb, byte expected) {
        byte readed = bb.get();
        if(readed != expected) {
            throw new FbException(String.format("Expected %X byte, but give: %X", expected, readed));
        }
    }

    static void clearDir(Path path) {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (attrs.isSymbolicLink()) {
                        // do not over symlinks
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return super.preVisitDirectory(dir, attrs);
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return super.visitFile(file, attrs);
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (!path.equals(dir)) {
                        // we remain the root dir
                        Files.delete(dir);
                    }
                    return super.postVisitDirectory(dir, exc);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Can not clean " + path);
        }
    }

    static void writeString(byte[] bytes, ByteBuffer to) {
        int len = bytes.length;
        Assert.isTrue(len < MAX_STR_LEN, "Too large byte string: " + Arrays.toString(bytes));
        to.put((byte) len);
        to.put(bytes, 0, len);
    }

    static byte[] toBytes(String str) {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        Assert.isTrue(bytes.length < MAX_STR_LEN, "Too large string: " + str);
        return bytes;
    }

    static String readString(byte[] tmp, ByteBuffer in) {
        int len = in.get() & 0xff;
        in.get(tmp, 0, len);
        return new String(tmp, 0, len, StandardCharsets.UTF_8);
    }

    public static void validate(String exp, String actual, String field) {
        if(exp.equals(actual)) {
            return;
        }
        differenceError(exp, actual, field);
    }

    public static void validate(long exp, long actual, String field) {
        if(exp == actual) {
            return;
        }
        differenceError(exp, actual, field);
    }

    public static void validate(int exp, int actual, String field) {
        if(exp == actual) {
            return;
        }
        differenceError(exp, actual, field);
    }

    private static void differenceError(Object exp, Object actual, String field) {
        throw new FbException("Read data has different '" + field + " expected=" + exp + " actual=" +actual);
    }
}
