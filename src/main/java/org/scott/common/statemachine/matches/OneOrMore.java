package org.scott.common.statemachine.matches;

import org.scott.common.statemachine.Input;
import org.scott.common.statemachine.Match;
import org.scott.common.statemachine.MatchAction;

/**
 * Created by IntelliJ IDEA.
 * User: N006719
 * Date: 13-Jul-2006
 * Time: 13:36:16
 * To change this template use File | Settings | File Templates.
 */
public class OneOrMore implements Match {
    char c;
    MatchAction action;
    boolean matchedOnce;
    boolean finished;

    public OneOrMore(char c, MatchAction action) {
        this.c = c;
        this.action = action;
    }

    public boolean matches(Input input) {
        while(!finished && input.hasMore()) {
            if (input.currentChar() == c) {
                matchedOnce = true;
            }
            else {
                finished = true;
                if (matchedOnce) {
                    input.backOne();
                }
            }
        }
        //we can't know for sure if we are finished
        if (matchedOnce && isFinished() && action != null) {
            action.perform(input); //perform the action when we have finshed matching
        }
        return matchedOnce; //we have matched as long as we have matched at least once
    }

    public void reset() {
        finished = false;
        matchedOnce = false;
    }

    public boolean isFinished() {
        return finished;
    }

}
