package edu.scut.cs.hm.model.node;

import edu.scut.cs.hm.docker.DockerService;
import edu.scut.cs.hm.common.mb.Subscriptions;
import org.springframework.security.acls.model.ObjectIdentity;

/**
 * Node registration in key-value store, default implementation act as proxy, therefore may be changed in another thread.
 */
public interface NodeRegistration {

    /**
     * Node information
     * @return
     */
    NodeInfo getNodeInfo();

    /**
     * Name of ngroup, can be null.
     * @return name or null
     */
    String getCluster();

    /**
     * Time for node registration in seconds.
     * @return seconds or negative value when is not applicable.
     */
    int getTtl();

    /**
     * Subscription of {@link NodeHealthEvent}
     * @return
     */
    Subscriptions<NodeHealthEvent> getHealthSubscriptions();

    /**
     * Object identity for Security ACL
     * @return
     */
    ObjectIdentity getOid();

    /**
     * Get Docker Service of this node
     * @return
     */
    DockerService getDocker();
}
