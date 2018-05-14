package edu.scut.cs.hm.model;

import edu.scut.cs.hm.admin.web.model.UiPermission;

public interface WithUiPermission {
    UiPermission getPermission();
    void setPermission(UiPermission permission);
}
