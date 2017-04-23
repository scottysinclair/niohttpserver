package org.scott.common.statemachine.matches;

import org.scott.common.statemachine.Input;
import org.scott.common.statemachine.Match;
import org.scott.common.statemachine.MatchAction;

/**
 * Created by IntelliJ IDEA.
 * User: N006719
 * Date: 13-Jul-2006
 * Time: 13:30:53
 * To change this template use File | Settings | File Templates.
 */
public class StringMatch implements Match {
    boolean matches;
    int pos;
    MatchAction action;
    String str;

    public StringMatch(String str, MatchAction action) {
        this.action = action;
        this.str = str;
        reset();
    }

    public boolean matches(Input input) {
        while(matches && input.hasMore() && !isFinished()) {
            matches = input.currentChar() == str.charAt(pos++);
        }
        if (action != null && isFinished() && matches) {
            action.perform(input);
        }
        return matches;
    }

    public boolean isFinished() {
        return pos >= str.length();
    }

    public void reset() {
        matches = true;
        pos = 0;
    }

}
