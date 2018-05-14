package edu.scut.cs.hm.admin.web.model.container;

import edu.scut.cs.hm.model.ContainerBaseIface;

import java.util.Date;

/**
 * Contract for any UI container representation
 */
public interface UiContainerIface extends ContainerBaseIface {
    void setId(String id);

    void setName(String name);

    void setImage(String image);

    void setImageId(String imageId);

    void setRun(boolean lock);
    void setLock(boolean lock);

    void setLockCause(String lockCause);

    String getStatus();
    void setStatus(String status);

    Date getCreated();
    void setCreated(Date date);
}
