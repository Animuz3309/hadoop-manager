package edu.scut.cs.hm.admin.web.model.user;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UiRoleUpdate extends UiRole {
    private boolean delete;
}