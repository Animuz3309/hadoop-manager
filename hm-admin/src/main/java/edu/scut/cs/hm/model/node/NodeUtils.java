package edu.scut.cs.hm.model.node;

import com.google.common.net.InternetDomainName;

/**
 * Node utils
 */
public final class NodeUtils {
    private NodeUtils() {
    }

    /**
     * Node name must be valid host name. In this method we check it.
     * @param name
     */
    public static void checkName(String name) {
        InternetDomainName.from(name);
    }
}
