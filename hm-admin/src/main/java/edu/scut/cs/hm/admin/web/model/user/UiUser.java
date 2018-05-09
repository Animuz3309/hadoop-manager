package edu.scut.cs.hm.admin.web.model.user;

import edu.scut.cs.hm.common.security.ExtendedUserDetails;
import edu.scut.cs.hm.common.security.MultiTenancySupport;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
public class UiUser extends UiUserBase {
    private List<UiRole> roles;

    public static UiUser fromDetails(UserDetails details) {
        UiUser user = new UiUser();
        String username = details.getUsername();
        user.setUsername(username);
        if(details instanceof ExtendedUserDetails) {
            ExtendedUserDetails eud = (ExtendedUserDetails) details;
            user.setTitle(eud.getTitle());
            user.setTenant(eud.getTenant());
            user.setEmail(eud.getEmail());
        }
        user.setPassword(details.getPassword() == null? null : PWD_STUB);
        Collection<? extends GrantedAuthority> authorities = details.getAuthorities();
        List<UiRole> roles = authorities.stream().map(UiRole::fromAuthority).sorted().collect(Collectors.toList());
        user.setRoles(roles);
        user.setTenant(MultiTenancySupport.getTenant(details));
        user.setAccountNonExpired(details.isAccountNonExpired());
        user.setAccountNonLocked(details.isAccountNonLocked());
        user.setCredentialsNonExpired(details.isCredentialsNonExpired());
        user.setEnabled(details.isEnabled());
        return user;
    }
}
