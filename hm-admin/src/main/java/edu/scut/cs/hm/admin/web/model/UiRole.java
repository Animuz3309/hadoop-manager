package edu.scut.cs.hm.admin.web.model;

import edu.scut.cs.hm.common.security.MultiTenancySupport;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.core.GrantedAuthority;

import javax.validation.constraints.NotNull;

@Data
public class UiRole implements Comparable<UiRole> {
    @NotNull
    private String name;
    private String tenant;

    public static UiRole fromAuthority(GrantedAuthority authority) {
        UiRole g = new UiRole();
        g.setName(authority.getAuthority());
        g.setTenant(MultiTenancySupport.getTenant(authority));
        return g;
    }

    @Override
    public int compareTo(UiRole o) {
        if (o == null) {
            return 1;
        }
        int compare = ObjectUtils.compare(this.tenant, o.tenant, false);
        if(compare == 0) {
            compare = ObjectUtils.compare(this.name, o.name, false);
        }
        return compare;
    }
}
