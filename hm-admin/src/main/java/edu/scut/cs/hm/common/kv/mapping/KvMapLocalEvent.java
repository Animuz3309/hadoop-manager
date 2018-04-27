package edu.scut.cs.hm.common.kv.mapping;

import lombok.Data;

@Data
public class KvMapLocalEvent<T> {
    private final KvMap<T> map;
    private final String key;
    private final T oldValue;
    private final T newValue;
    private final Action action;

    public enum Action {
        /**
         * save new value
         */
        CREATE,
        /**
         * save value over existed
         */
        UPDATE,
        /**
         * Delete existed value
         */
        DELETE,
        /**
         * Load existed value
         */
        LOAD
    }
}
