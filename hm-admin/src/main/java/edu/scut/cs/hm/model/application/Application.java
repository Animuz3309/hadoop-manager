package edu.scut.cs.hm.model.application;

import edu.scut.cs.hm.model.Named;

import java.util.Date;
import java.util.List;

/**
 * Common iface for application instance. <p/>
 */
public interface Application extends Named {

    /**
     * Identifier of deployed instance.
     * @return
     */
    String getName();

    String getCluster();

    String getInitFile();

    Date getCreatingDate();

    List<String> getContainers();
}
