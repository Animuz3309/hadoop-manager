package edu.scut.cs.hm.common.fc;

import java.util.function.Consumer;

/**
 * Snapshot which alloc readonly operations
 * @param <E>
 */
public interface FbSnapshot<E> extends AutoCloseable {
    void visit(int offset, Consumer<E> consumer);
}
