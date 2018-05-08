package edu.scut.cs.hm.admin.service;

import edu.scut.cs.hm.common.kv.KvUtils;
import edu.scut.cs.hm.common.kv.mapping.KvMap;
import edu.scut.cs.hm.common.kv.mapping.KvMapAdapter;
import edu.scut.cs.hm.common.kv.mapping.KvMapperFactory;
import edu.scut.cs.hm.common.security.ExtendedUserDetails;
import edu.scut.cs.hm.common.security.ExtendedUserDetailsImpl;
import edu.scut.cs.hm.common.security.UserIdentifiers;
import edu.scut.cs.hm.common.security.UserIdentifiersDetailsService;
import edu.scut.cs.hm.common.utils.ExtendedAssert;
import edu.scut.cs.hm.model.user.UserRegistration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Service to manage user, use k-v storage
 */
@Getter
@Slf4j
@Service
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UserStorage implements UserIdentifiersDetailsService {

    private final KvMap<UserRegistration> map;
    private final AccessDecisionManager adm;

    @Autowired
    public UserStorage(KvMapperFactory mapperFactory, AccessDecisionManager accessDecisionManager) {
        this.adm = accessDecisionManager;
        String prefix = KvUtils.join(mapperFactory.getStorage().getPrefix(), "users");
        this.map = KvMap.builder(UserRegistration.class, ExtendedUserDetailsImpl.class)
                .mapper(mapperFactory)
                .path(prefix)
                .passDirty(true)
                .adapter(new KvMapAdapterImpl())
                .build();
    }

    @PostConstruct
    public void init() {
        load();
    }

    private void load() {
        map.load();
    }

    @Override
    public Collection<ExtendedUserDetails> getUsers() {
        Collection<UserRegistration> values = map.values();
        return values.stream().map(UserRegistration::getDetails).collect(Collectors.toList());
    }

    @Override
    public ExtendedUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserRegistration ur = this.map.get(username);
        if (ur == null) {
            return null;
        }
        return ur.getDetails();
    }

    @Override
    public ExtendedUserDetails loadUserByIdentifiers(UserIdentifiers identifiers) {
        UserRegistration ur= this.map.values().stream()
                .filter(u -> u.match(identifiers))
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("Identifiers: " + identifiers));
        return ur.getDetails();
    }

    public UserRegistration remove(String name) {
        return map.remove(name);
    }

    public UserRegistration update(String name, Consumer<UserRegistration> consumer) {
        return map.compute(name, (k, ur) -> {
            if (ur == null) {
                ur = new UserRegistration(this, k);
            }
            ur.update(consumer);
            // when update is end without details
            if (ur.getDetails() == null) {
                // we remove it
                return null;
            }
            return ur;
        });
    }

    public UserRegistration get(String name) {
        ExtendedAssert.matchAz09Hyp(name, "user name");
        return map.get(name);
    }

    private class KvMapAdapterImpl implements KvMapAdapter<UserRegistration> {

        /**
         * Retrieve object from source for saving
         *
         * @param key
         * @param source source object, cannot be null
         * @return null is not allowed
         */
        @Override
        public Object get(String key, UserRegistration source) {
            return source.getDetails();
        }

        /**
         * Set loaded object to source object
         *
         * @param key
         * @param source old source object maybe null
         * @param value  loaded value, null not allowed
         * @return new source object, also you can return 'old source'
         */
        @Override
        public UserRegistration set(String key, UserRegistration source, Object value) {
            if (source == null) {
                source = new UserRegistration(UserStorage.this, key);
            }
            source.loadDetails((ExtendedUserDetails) value);
            return source;
        }
    }
}
