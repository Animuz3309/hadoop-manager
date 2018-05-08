package edu.scut.cs.hm.model.user;

import edu.scut.cs.hm.admin.service.UserStorage;
import edu.scut.cs.hm.common.kv.mapping.KvMap;
import edu.scut.cs.hm.common.kv.mapping.KvMapping;
import edu.scut.cs.hm.common.security.Authorities;
import edu.scut.cs.hm.common.security.ExtendedUserDetails;
import edu.scut.cs.hm.common.security.ExtendedUserDetailsImpl;
import edu.scut.cs.hm.common.security.UserIdentifiers;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Hold user details and persist its into k-v storage
 * By default does not has any details (it prevent publication of invalid details).
 */
public class UserRegistration {
    private final Object lock = new Object();
    private final UserStorage storage;
    private final KvMap<UserRegistration> map;
    private final String name;
    @KvMapping
    private ExtendedUserDetailsImpl details;

    public UserRegistration(UserStorage storage, String name) {
        this.storage = storage;
        this.map = storage.getMap();
        this.name = name;
    }

    /**
     *
     * @return detail or null
     */
    public ExtendedUserDetails getDetails() {
        synchronized (lock) {
            return details;
        }
    }

    public void setDetails(ExtendedUserDetails details) {
        synchronized (lock) {
            ExtendedUserDetailsImpl changed = ExtendedUserDetailsImpl.from(details);
            validate(changed);
            checkAccess(changed);
            this.details = changed;
        }
    }

    private void checkAccess(ExtendedUserDetailsImpl changed) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(details == null ||
                !details.getAuthorities().equals(changed.getAuthorities()) ||
                !auth.getName().equals(this.name)) {
            // change authorities of user can do only global or tenant admin
            List<ConfigAttribute> authorities = Collections.singletonList(Authorities.fromName(Authorities.ADMIN_ROLE, this.details == null? null : this.details.getTenant()));
            this.storage.getAdm().decide(auth, this.details, authorities);
        }
    }

    /**
     * load and normalise uer details
     * @param details details
     */
    public void loadDetails(ExtendedUserDetails details) {
        synchronized (lock) {
            if (details != null && !this.name.equals(details.getUsername())) {
                details = ExtendedUserDetailsImpl.builder(details).username(name).build();
            }
            this.details = ExtendedUserDetailsImpl.from(details);
        }
    }

    public boolean match(UserIdentifiers identifiers) {
        synchronized (lock) {
            String username = identifiers.getUsername();
            String email = identifiers.getEmail();
            return (username == null || username.equals(name)) &&
                    (email == null || email.equals(details.getEmail()));
        }
    }

    /**
     * Invoke consumer in local lock.
     * @param consumer
     */
    public void update(Consumer<UserRegistration> consumer) {
        synchronized (lock) {
            consumer.accept(this);
            map.flush(name);
        }
    }

    private void validate(ExtendedUserDetails another) {
        if(!this.name.equals(another.getUsername())) {
            throw new IllegalArgumentException("Changing of name (orig:" + this.name
                    + ", new:" + another.getUsername() + ") is not allowed.");
        }
        String anotherTenant = another.getTenant();
        if(anotherTenant == null) {
            throw new IllegalArgumentException("tenant is null");
        }
        if(this.details != null) {
            // in some cases user may have null tenant (it user is corrupted), and we must lave way to fix it through ui
            String oldTenant = this.details.getTenant();
            if(oldTenant != null && !oldTenant.equals(anotherTenant)) {
                throw new IllegalArgumentException("Change of tenant (orig:" + oldTenant
                        + ", new:" + anotherTenant + ") is not allowed.");
            }
        }
    }
}
