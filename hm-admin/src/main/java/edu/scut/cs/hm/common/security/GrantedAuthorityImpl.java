package edu.scut.cs.hm.common.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;

import java.beans.ConstructorProperties;

/**
 * Implementation of granted authority with tenant, configAttribute support <p/>
 * Spring Security use string to represent the 'Authority'
 * @see edu.scut.cs.hm.common.security.Tenant
 * @see org.springframework.security.access.ConfigAttribute
 */
@ToString
@EqualsAndHashCode
@Getter
public class GrantedAuthorityImpl implements TenantGrantedAuthority {
    // 代表了tenant对象的identify 就是Tenant#getName()
    private final String tenant;
    private final String authority;

    /**
     * Wrapper a {@link GrantedAuthority} object to customer {@link GrantedAuthorityImpl} object
     * @param authority
     * @return
     */
    public static GrantedAuthorityImpl from(GrantedAuthority authority) {
        return new GrantedAuthorityImpl(authority.getAuthority(), MultiTenancySupport.getTenant(authority));
    }

    /**
     * Convert a {@link GrantedAuthority} object to customer {@link GrantedAuthorityImpl} object
     * @param authority
     * @return
     */
    public static GrantedAuthority convert(GrantedAuthority authority) {
        if (authority instanceof GrantedAuthorityImpl) {
            return authority;
        }
        return from(authority);
    }

    /**
     *
     * @param authority name of authority
     * @param tenant {@link Tenant}'s identify (Tenant's name)
     */
    @JsonCreator
    // 使用参数名注入，外部指定tenant=1, 根据如下注解的指示，spring将tenant=1注入到构造函数的第二个参数
    // note. runtime时候是获取不到方法形参的名的(parameter name)
    @ConstructorProperties({"authority", "tenant"})
    public GrantedAuthorityImpl(@JsonProperty("authority") String authority,
                                @JsonProperty("tenant") String tenant) {
        this.tenant = tenant;
        this.authority = authority;
    }

    /**
     * Stores a security system related configuration attribute.
     * @see org.springframework.security.access.ConfigAttribute
     * Here is the authority
     * @return
     */
    @JsonIgnore
    @Override
    public String getAttribute() {
        return authority;
    }
}
