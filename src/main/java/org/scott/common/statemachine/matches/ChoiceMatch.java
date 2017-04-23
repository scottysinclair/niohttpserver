package org.scott.common.statemachine.matches;

import org.scott.common.statemachine.Input;
import org.scott.common.statemachine.Match;

/**
 * Created by IntelliJ IDEA.
 * User: N006719
 * Date: 13-Jul-2006
 * Time: 13:34:49
 * To change this template use File | Settings | File Templates.
 */
public class ChoiceMatch implements Match {
    int startPos;
    Match totry[];
    int match;
    boolean matches;

    public ChoiceMatch(Match[] totry) {
        this.totry = totry;
        reset();
    }

    public boolean matches(Input input) {
        while(matches && !totry[match].isFinished() && input.hasMore()) {
            if (startPos == -1) {
                startPos = input.getPosition();
            }
            matches = totry[match].matches(input);
            if (!matches) {
                while(nextChoice() && ! (matches = catchUp(input)));
            }
        }
        return matches;
    }

    public void reset() {
        for (int i=0; i<totry.length; i++) {
            totry[i].reset();
        }
        match = 0;
        startPos = -1;
        matches = true;
    }

    private boolean nextChoice() {
        match++;
        return match < totry.length;
    }

    private boolean catchUp(Input input) {
        int topos = input.getPosition();
        input.gotoPosition(startPos);
        for (int i=startPos; i<topos; i++) {
            if (!totry[match].matches(input)) {
                return false;
            }
        }
        return true;
    }

    public boolean isFinished() {
        return match >= totry.length || totry[match].isFinished();
    }

}
