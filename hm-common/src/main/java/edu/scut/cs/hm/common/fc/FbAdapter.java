package edu.scut.cs.hm.common.fc;

import java.io.IOException;

public interface FbAdapter<T> {
    byte[] serialize(T obj) throws IOException;
    T deserialize(byte[] data, int offset, int len) throws IOException;
}
