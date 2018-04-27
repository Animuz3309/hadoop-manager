package edu.scut.cs.hm.common.kv.mapping;

import edu.scut.cs.hm.common.kv.KvStorageEvent;
import lombok.Data;

/**
 * Remote map event, mapped from @{@link edu.scut.cs.hm.common.kv.KvStorageEvent},
 * do not confuse with {@link }
 */
@Data
public class KvMapEvent<T> {
    private final KvMap<T> map;
    private final String key;
    /**
     * Value can be null, if in not present in local cache.
     */
    private final T value;
    private final KvStorageEvent.Crud action;
}
