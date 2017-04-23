package org.scott.common.statemachine.matches;

import org.scott.common.statemachine.Input;
import org.scott.common.statemachine.Match;

/**
 * Created by IntelliJ IDEA.
 * User: N006719
 * Date: 13-Jul-2006
 * Time: 14:14:31
 * To change this template use File | Settings | File Templates.
 */
public class MatchUntil implements Match {
    Match match;
    public MatchUntil(Match match) {
        this.match = match;
        reset();
    }

    public boolean matches(Input input) {
        boolean matches = false;
        while(!matches && !isFinished() && input.hasMore()) {
            matches = match.matches(input);
            if (!matches) {
              match.reset();
            }
//            else {
//               input.backOne();
//            }
        }
        return true;
    }

    public boolean isFinished() {
        return match.isFinished();
    }

    public void reset() {
        match.reset();
    }
}
