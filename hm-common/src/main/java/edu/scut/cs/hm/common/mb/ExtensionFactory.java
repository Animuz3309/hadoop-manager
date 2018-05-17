package edu.scut.cs.hm.common.mb;

import edu.scut.cs.hm.common.utils.Key;

/**
 * @see Subscriptions#getOrCreateExtension(Key, ExtensionFactory)
 */
public interface ExtensionFactory<T, M> {
    T create(Key<T> key, MessageBus<M> bus);
}
