package edu.scut.cs.hm.common.security.acl.dto;


import org.springframework.security.acls.domain.AclFormattingUtils;
import org.springframework.security.acls.model.Permission;

/**
 * Customer implements Spring Securit Acl <code>Permission</code> for action
 * <p>
 *     Particular type of permission to do with docker containers, physical swarmNode and so on,
 *     we use 6-bits to represent 6 permissions
 * </p>
 */
public enum Action implements Permission {
    READ(0),
    UPDATE(1),
    CREATE(2),
    DELETE(3),
    EXECUTE(4),
    /**
     * Permission to change internal structure or enclosing items in docker container <p/>
     * like install hadoop.jar in a container
     */
    ALTER_INSIDE(5),
    ;

    private final int mask;
    private final char c;

    Action(int position) {
        this.mask = 1 << position;
        c = name().charAt(0);
    }

    /**
     * Letter that identity action
     * @return
     */
    public char getLetter() { return c; }

    public int getMask() {
        return mask;
    }

    @Override
    public String getPattern() {
        return AclFormattingUtils.printBinary(mask, c);
    }

    public static Action fromLetter(char c) {
        for (Action action: values()) {
            if (action.getLetter() == c) {
                return action;
            }
        }
        return null;
    }
}
