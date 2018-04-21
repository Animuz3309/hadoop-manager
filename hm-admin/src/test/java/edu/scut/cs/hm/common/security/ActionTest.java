package edu.scut.cs.hm.common.security;

import edu.scut.cs.hm.common.security.acl.dto.Action;
import org.junit.Test;

import static org.junit.Assert.*;

public class ActionTest {

    @Test
    public void getPattern() {
        Action action = Action.fromLetter('R');
        assert action != null;
        assertEquals("...............................R", action.getPattern());
    }

    @Test
    public void fromLetter() {
        Action action = Action.fromLetter('R');
        assertEquals(Action.READ, Action.fromLetter('R'));
    }
}