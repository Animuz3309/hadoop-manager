package edu.scut.cs.hm.docker;

import edu.scut.cs.hm.docker.arg.GetEventsArg;
import edu.scut.cs.hm.docker.res.ServiceCallResult;

/**
 * Docker client API
 */
public interface DockerService {
    String DS_PREFIX = "ds:";

    /**
     * Name of cluster
     * Note: only one of {@link #getCluster()} or {@link #getNode()} can has non null value
     * @return
     */
    String getCluster();

    /**
     * Name of node
     * ote: only one of {@link #getCluster()} or {@link #getNode()} can has non null value
     * @return
     */
    String getNode();

    /**
     * Return Id of Docker Service
     * @return
     */
    default String getId() {
        StringBuilder sb = new StringBuilder(DS_PREFIX);
        String cluster = getCluster();
        if (cluster != null) {
            sb.append("cluster:").append(cluster);
        } else {
            sb.append("node:").append(getNode());
        }
        return sb.toString();
    }

    /**
     * Address of docker service in 'ip:port' format, sometime it can be null.
     * @return address or null
     */
    String getAddress();

    /**
     * Docker service is online or not
     * @return
     */
    boolean isOnline();

    /**
     * Subscribe a watcher in GetEventsArg to Docker event api
     * @param arg
     * @return
     */
    ServiceCallResult subscribeToEvents(GetEventsArg arg);
}
