package edu.scut.cs.hm.common.kv;

import edu.scut.cs.hm.common.mb.ConditionalSubscriptions;

import java.util.List;
import java.util.Map;

/**
 * key-value store for shared configuration and service discovery
 */
public interface KeyValueStorage {

    /**
     * Get the value of a key
     * @param key the key
     * @return  the node with corresponding value or null when not found value
     */
    KvNode get(String key);

    /**
     * Setting the value of a key without options
     * @param key
     * @param value
     * @return
     */
    default KvNode set(String key, String value) {
        return set(key, value, null);
    }

    /**
     * Setting the value of a key with options
     * @param key
     * @param value
     * @param ops
     * @return
     */
    KvNode set(String key, String value, WriteOptions ops);

    /**
     * Make or update a directory at specified key
     * @param key
     * @param ops ops or null
     * @return
     */
    KvNode setDir(String key, WriteOptions ops);

    /**
     * Delete a directory at specified key
     * @param key
     * @param ops ops or null
     * @return
     */
    KvNode delDir(String key, DeleteDirOptions ops);

    /**
     * Delete a key
     * @param key
     * @param ops ops or null
     * @return
     */
    KvNode delete(String key, DeleteDirOptions ops);

    /**
     * List keys of specified prefix
     * @param prefix the prefix of keys
     * @return list or null if key is absent
     */
    List<String> list(String prefix);

    /**
     * Retrieve map of keys and its values from specified prefix
     * @param prefix the prefix of keys
     * @return
     */
    Map<String, String> map(String prefix);

    /**
     * Return Subscriptions for key value event of this storage. <p/>
     * Note that subscription may be on '/key' - or on key with its childs '/key*' (also '/key/*')
     * @return
     */
    ConditionalSubscriptions<KvStorageEvent, String> subscriptions();

    /**
     * Get the prefix of key
     * @return
     */
    String getPrefix();
}
