package edu.scut.cs.hm.admin.web.model;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@Data
public class UiUserBase {
    /**
     * Stub for any non null password
     */
    public static final String PWD_STUB = "********";
    @NotNull
    private String username;
    private String title;
    private String email;
    private String tenant;
    @NotNull
    @Length(min = 3)
    private String password;
    private Boolean accountNonExpired;
    private Boolean accountNonLocked;
    private Boolean credentialsNonExpired;
    private Boolean enabled;
}
