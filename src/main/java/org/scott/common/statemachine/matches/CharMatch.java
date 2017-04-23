package org.scott.common.statemachine.matches;

import org.scott.common.statemachine.Input;
import org.scott.common.statemachine.Match;
import org.scott.common.statemachine.MatchAction;

/**
 * Created by IntelliJ IDEA.
 * User: N006719
 * Date: 13-Jul-2006
 * Time: 13:33:36
 * To change this template use File | Settings | File Templates.
 */
public class CharMatch implements Match {
    boolean called = false;
    char c;
    MatchAction action;

    public CharMatch(char c, MatchAction action) {
        this.c = c;
        this.action = action;
    }


    public boolean matches(Input input) {
        called = true;
        boolean b = input.currentChar() == c;
        if (b && action != null) {
            action.perform(input);
        }
        return b;
    }

    public void reset() {
        called = false;
    }

    public boolean isFinished() {
        return called;
    }

}
