package edu.scut.cs.hm.common.security.acl.dto;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.acls.domain.AclFormattingUtils;
import org.springframework.security.acls.model.Permission;

/**
 * Permission DTO
 * @see org.springframework.security.acls.model.Permission
 */
@Getter
@EqualsAndHashCode(of = "mask")
public class PermissionData implements Permission {

    @Getter
    @EqualsAndHashCode(of = "mask")
    public static class Builder implements Permission {
        private String pattern = THIRTY_TWO_RESERVED_OFF;
        private int mask;

        public Builder remove(Permission permission) {
            this.mask &= ~permission.getMask();
            this.pattern = AclFormattingUtils.demergePatterns(this.pattern, permission.getPattern());
            return this;
        }

        public Builder clear() {
            this.mask = 0;
            this.pattern = THIRTY_TWO_RESERVED_OFF;
            return this;
        }

        public Builder add(Permission permission) {
            this.mask |= permission.getMask();
            this.pattern = AclFormattingUtils.mergePatterns(this.pattern, permission.getPattern());
            return this;
        }

        public Builder add(Permission... permissions) {
            for (Permission permission: permissions) {
                add(permission);
            }
            return this;
        }

        public final String toString() {
            return this.getClass() + "[" + getPattern() + "=" + mask + "]";
        }

        public PermissionData build() {
            return new PermissionData(pattern, mask);
        }
    }

    /**
     * Has all permissions of action defined in {@link edu.scut.cs.hm.common.security.acl.dto.Action}
     */
    public static final PermissionData ALL = PermissionData.builder().add((Permission[]) Action.values()).build();
    /**
     * Has not permission of action defined in {@link edu.scut.cs.hm.common.security.acl.dto.Action}
     */
    public static final PermissionData NONE = builder().build();

    private final String expression;    // permission represent Action letter in order to for human reading
    private final String pattern;
    private final int mask;

    /**
     * Jackjson use this method to deserialize from string
     * @param expression
     * @return
     */
    @JsonCreator
    public static PermissionData from(String expression) {
        int mask = 0;
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            Action action = Action.fromLetter(c);
            if (action == null) {
                throw new IllegalArgumentException("Unknown action letter: " + c +
                        " in permission expression: " + expression);
            }
            mask |= action.getMask();
        }
        return new PermissionData(null, mask);
    }

    /**
     * Create PermissionData from implementation of {@link org.springframework.security.acls.model.Permission}
     * @param permission
     * @return
     */
    public static PermissionData from(Permission permission) {
        if(permission == null  || permission instanceof PermissionData) {
            return (PermissionData) permission;
        }
        return new PermissionData(permission.getPattern(), permission.getMask());
    }

    public static Builder builder() {
        return new Builder();
    }

    public PermissionData(String pattern, int mask) {
        this.mask = mask;
        this.pattern = pattern != null ? pattern : AclFormattingUtils.printBinary(mask);

        Action[] actions = Action.values();
        StringBuilder sb = new StringBuilder(actions.length);
        for (Action a: actions) {
            if ((a.getMask() & this.mask) != 0) {
                sb.append(a.getLetter());
            }
        }
        this.expression = sb.toString();
    }

    public boolean has(Permission permission) {
        int req = permission.getMask();
        return (this.mask & req) == req;
    }

    @JsonIgnore
    @Override
    public String getPattern() {
        return pattern;
    }

    @JsonValue
    public String getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        return getExpression();
    }
}
