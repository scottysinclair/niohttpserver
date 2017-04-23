package org.scott.common.statemachine;

/**
 * Created by IntelliJ IDEA.
 * User: N006719
 * Date: 13-Jul-2006
 * Time: 13:31:25
 * To change this template use File | Settings | File Templates.
 */
public class MatchStream implements Match {
    protected Match matchStream[];
    private int currentPos;
    private boolean matches;
    private boolean jumped;

    public MatchStream() {
        reset();
    }

    public boolean matches(Input input) {
        while(input.hasMore() && matches && !isFinished()) {
            Match currentMatch = matchStream[currentPos];
            matches = currentMatch.matches(input);
            if (matches && currentMatch.isFinished() && !jumped) {
                nextMatch();
            }
            jumped = false;
        }
        return matches;
    }

    private void nextMatch() {
        currentPos++;
        if (currentPos < matchStream.length) {
            matchStream[currentPos].reset();
        }
    }
    public void jumpto(int position) {
        currentPos = position;
        if (currentPos < matchStream.length) {
            matchStream[currentPos].reset();
        }
        jumped = true;
    }

    public void backOne() {
        currentPos--;
    }

    public boolean isFinished() {
        return !matches || currentPos >= matchStream.length;
    }

    public void reset() {
        if (matchStream != null) {
            for (int i=0; i<matchStream.length; i++) {
                matchStream[i].reset();
            }
        }
        currentPos = 0;
        matches = true;
    }
}
