package edu.scut.cs.hm.admin.security.acl;

import edu.scut.cs.hm.common.security.acl.dto.AclSource;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * Provide/Update acl source for specified object id.
 */
public interface AclProvider {
    /**
     * Provide acl source for specified object id.
     * @param id
     * @return
     */
    AclSource provide(Serializable id);

    /**
     * Update acl source for specified object id.
     * @param id
     * @param operator operator to modify AclSource.Builder
     */
    void update(Serializable id, AclModifier operator);

    /**
     * List acl source
     * @param consumer
     */
    void list(Consumer<AclSource> consumer);
}
