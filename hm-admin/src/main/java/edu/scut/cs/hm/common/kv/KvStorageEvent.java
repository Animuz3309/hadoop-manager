package edu.scut.cs.hm.common.kv;

import lombok.Data;

/**
 * Event of Key-value storage
 */
@Data
public class KvStorageEvent {

    /**
     * index of node, like modifiedIndex in etcd
     * just like an long unique id of node
     */
    private final long index;
    private final String key;
    private final String value;
    private final long ttl;
    private final Crud action;

    public enum Crud {
        CREATE, READ, UPDATE, DELETE
    }
}
