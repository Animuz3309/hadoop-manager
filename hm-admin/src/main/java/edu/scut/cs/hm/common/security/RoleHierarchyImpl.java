package edu.scut.cs.hm.common.security;

import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of {@link org.springframework.security.access.hierarchicalroles.RoleHierarchy}
 * and support to get all defined authorities in the role hierarchy authority tree
 */
public class RoleHierarchyImpl implements RoleHierarchy, AuthoritiesService {

    public static class Builder {
        private final Map<String, Set<String>> map = new HashMap<>();

        public RoleHierarchyImpl build() {
            return new RoleHierarchyImpl(this);
        }

        /**
         * Add 'role -> {child1, child2, ...}' relation
         * @param role  parent role
         * @param childs child roles
         * @return
         */
        public Builder childs(String role, String... childs) {
            getSet(role).addAll(Arrays.asList(childs));
            return this;
        }

        private Set<String> getSet(String parent) {
            return map.computeIfAbsent(parent, s -> new HashSet<>());
        }
    }

    private final Map<String, Set<String>> map;
    private final Collection<GrantedAuthority> allAuthorityes;

    public static Builder builder() {
        return new Builder();
    }

    private RoleHierarchyImpl(Builder b) {
        Map<String, Set<String>> src = b.map;

        // grant all defined authorities
        // include parent in key, and child in value
        Set<String> all = new HashSet<>(src.keySet());
        src.values().forEach(all::addAll);

        // store authorities
        this.allAuthorityes = Collections.unmodifiableList(
                all.stream().map(Authorities::fromName).collect(Collectors.toList()));

        // build map 'authority' -> 'all its child'
        Map<String, Set<String>> dst = new HashMap<>();
        for (String authority: all) {
            Set<String> dstChilds = new HashSet<>();
            addChilds(src, authority, dstChilds);
            dst.put(authority, Collections.unmodifiableSet(dstChilds));
        }
        this.map = Collections.unmodifiableMap(src);
    }

    private static void addChilds(Map<String, Set<String>> src, String authority, Set<String> dst) {
        Set<String> childs = src.get(authority);
        if (childs == null) {
            return;
        }
        for (String child: childs) {
            if (dst.add(child)) {
                // add child's child to dst
                addChilds(src, child, dst);
            }
        }
    }

    /**
     * return all authorities defined
     * @return
     */
    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return allAuthorityes;
    }

    /**
     * return reachable granted authorities of specific authorities given (include child)
     * @param authorities
     * @return
     */
    @Override
    public Collection<? extends GrantedAuthority>
        getReachableGrantedAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Set<GrantedAuthority> res = new HashSet<>(authorities);
        for (GrantedAuthority authority: authorities) {
            String tenant = MultiTenancySupport.getTenant(authority);
            Set<String> childs = map.get(authority.getAuthority());
            if (childs == null) {
                continue;
            }
            childs.stream().map(a -> new GrantedAuthorityImpl(a, tenant)).forEach(res::add);
        }
        return Collections.unmodifiableSet(res);
    }
}
