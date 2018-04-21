package edu.scut.cs.hm.common.security;

/**
 * Domain object owner's identity, a tenant means '租户' in chinese
 * <p>
 *     Like Customer or Staff in a "Shop Application", but not represent a specified customer named 'Bob'
 *     or 'Staff' named 'Mike'.
 *     in other words, 'Tenant' is similar to 'group' in linux system,
 *     e.g. 'root' is a probably value {@link #getName()}
 *     return
 * </p>
 */
public interface Tenant {
    String getName();
}
