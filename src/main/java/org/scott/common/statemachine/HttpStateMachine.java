package org.scott.common.statemachine;

import org.scott.common.statemachine.matches.*;


/**
 * Created by IntelliJ IDEA.
 * User: N006719
 * Date: 12-Jul-2006
 * Time: 16:08:14
 * To change this template use File | Settings | File Templates.
 */
public class HttpStateMachine extends MatchStream {
    private HttpRequestParserHandler requestHandler;
    private String paramName;
    private String headerName;

    public HttpStateMachine() {
        initVersion2();
    }

    private void initVersion1(){
        matchStream = new Match[]{
                choiceMatch(charMatch('G'), charMatch('P', jumpTo(3, this))),
                stringMatch("ET", setMethod("GET")),
                oneOrMore(' ', actions(clearBuffer(), jumpTo(5, this))),
                stringMatch("OST", setMethod("POST")),
                oneOrMore(' ', clearBuffer()),

                matchUntil(choiceMatch(oneOrMore(' ', jumpTo(8, this)), charMatch('?', getPath()))),
                matchUntil(choiceMatch(charMatch('=', paramName()), oneOrMore(' ',  actions(paramName(), setParam())))),
                matchUntil(choiceMatch(charMatch('&', actions(setParam(), jumpTo(6, this))), oneOrMore(' ',  setParam()))),

                stringMatch("HTTP/1."),
                choiceMatch(charMatch('1'), charMatch('0')),
                matchUntil(charMatch('\n'))
        };
    }

    private void initVersion2() {
        matchStream = new Match[]{
             new ChooseMethod(),
             new Get(),
             new MatchAnyWhiteSpaces(5),
             new Post(),
             new MatchAnyWhiteSpaces(5),
             new Path(),
             new MatchAnyWhiteSpaces(10),
             new QueryParamName(),
             new QueryParamValue(),
             new MatchAnyWhiteSpaces(),
             new ProtocolVersion(),
             new MatchToNewline(),
             new FinishedOrHeader(),
             new HeaderName(),
             new MatchAnyWhiteSpaces(),
             new HeaderValue(),
        };
    }

    public void setRequestHandler(HttpRequestParserHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    private MatchAction actions(final MatchAction a, final MatchAction b) {
        return new MatchAction() {
            public void perform(Input input) {
                a.perform(input);
                b.perform(input);
            }
        };
    }

    private MatchAction getPath() {
        return new MatchAction() {
            public void perform(Input input) {
                String str = input.sofar().consume();
                requestHandler.setPath(str.substring(0, str.length()-1));
            }
        };
    }

    private MatchAction paramName() {
        return new MatchAction() {
            public void perform(Input input) {
                paramName = input.sofar().consume();
                paramName = paramName.substring(0, paramName.length()-1);
            }
        };
    }

    private MatchAction setParam() {
        return new MatchAction() {
            public void perform(Input input) {
                String paramValue = input.sofar().consume();
                paramValue = paramValue.substring(0, paramValue.length()-1);
                requestHandler.setQueryParam(paramName, paramValue);
            }
        };
    }

    private MatchAction clearBuffer() {
        return new MatchAction() {
            public void perform(Input input) {
                input.sofar().consume();
            }
        };
    }

    private MatchAction setMethod(final String methodName) {
        return new MatchAction() {
            public void perform(Input input) {
                requestHandler.setMethod(methodName);
            }
        };
    }

    private MatchAction jumpTo(final int position, final MatchStream ms) {
        return new MatchAction() {
            public void perform(Input input) {
                ms.jumpto(position);
            }
        };
    }

    private Match matchUntil(final Match match) {
        return new MatchUntil(match);
    }

    private Match stringMatch(String str) {
        return new StringMatch(str, null);
    }
    private Match stringMatch(String str, MatchAction action) {
        return new StringMatch(str, action);
    }

    private Match charMatch(char c, MatchAction action) {
        return new CharMatch(c, action);
    }
    private Match charMatch(char c) {
        return new CharMatch(c, null);
    }

    private Match choiceMatch(Match a, Match b) {
        return new ChoiceMatch(new Match[]{a,b});
    }


    private Match oneOrMore(final char c, final MatchAction action) {
        return new OneOrMore(c, action);
    }

    class ChooseMethod implements Match {
        private boolean finished;
        public boolean matches(Input input) {
            switch(input.currentChar()) {
                case 'G':  return finished = true;
                case 'P': jumpto(2); return finished = true;
                default: return !(finished = true);
            }
        }

        public boolean isFinished() {
            return finished;
        }

        public void reset() {
            finished = false;
        }
    }

    class Get implements Match {
        private boolean finished;
        private int count = 0;

        public boolean matches(Input input) {
            switch(count++) {
                case 0: return input.currentChar() == 'E';
                case 1: return input.currentChar() == 'T';
                case 2: requestHandler.setMethod("GET"); finished = true; return input.currentChar() == ' ';
                default: return false;
            }
        }

        public boolean isFinished() {
            return finished;
        }

        public void reset() {
            count = 0;
            finished = false;
        }
    }

    class Post implements Match {
        private boolean finished;
        private int count = 0;

        public boolean matches(Input input) {
            switch(count++) {
                case 0: return input.currentChar() == 'O';
                case 1: return input.currentChar() == 'S';
                case 2: return input.currentChar() == 'T';
                case 3: if (input.currentChar() == ' ') {
                            requestHandler.setMethod("POST");
                            finished = true;
                            return true;
                        }
                        else {
                           return false;
                        }
                default: return false;
            }
        }

        public boolean isFinished() {
            return finished;
        }

        public void reset() {
            count = 0;
            finished = false;
        }
    }

    class MatchAnyWhiteSpaces implements Match {
        private boolean finished;
        private int jumpTo;

        public MatchAnyWhiteSpaces() {
            this(-1);
        }

        public MatchAnyWhiteSpaces(int jumpTo) {
            this.jumpTo = jumpTo;
        }

        public boolean matches(Input input) {
            char c = input.currentChar();
            if (c != ' ' && c != '\t') {
                input.backOne();
                input.sofar().consume();
                if (jumpTo != -1) {
                    jumpto(jumpTo);
                }
                finished = true;
            }
            return true;
        }

        public boolean isFinished() {
            return finished;
        }

        public void reset() {
            finished = false;
        }
    }

    class Path implements Match {
        private boolean finished;
        public boolean matches(Input input) {
            switch(input.currentChar()) {
                case ' ': setPath(input);return finished = true;
                case '?':  setPath(input); jumpto(7); return finished = true;
                default: return true;
            }
        }

        private void setPath(Input input) {
            String str = input.sofar().consume();
            requestHandler.setPath(str.substring(0, str.length()-1));
        }

        public boolean isFinished() {
            return finished;
        }

        public void reset() {
            finished = false;
        }
    }

    class QueryParamName implements Match {
        private boolean finished;
        public boolean matches(Input input) {
            switch(input.currentChar()) {
                case '=':
                case '&':
                case ' ': setParam(input); return finished = true;
                default: return true;
            }
        }

        private void setParam(Input input) {
            String str = input.sofar().consume();
            paramName = str.substring(0, str.length()-1);
        }

        public boolean isFinished() {
            return finished;
        }

        public void reset() {
            finished = false;
        }
    }

    class QueryParamValue implements Match {
        private boolean finished;
        public boolean matches(Input input) {
            switch(input.currentChar()) {
                case '&':  setParam(input); jumpto(7); return finished = true;
                case ' ':  setParam(input); return finished = true;
                default: return true;
            }
        }

        private void setParam(Input input) {
            String str = input.sofar().consume();
            requestHandler.setQueryParam(paramName, str.substring(0, str.length()-1));
        }

        public boolean isFinished() {
            return finished;
        }

        public void reset() {
            finished = false;
        }
    }

    class ProtocolVersion implements Match {
        private boolean finished = false;
        private int count = 0;
        public boolean matches(Input input) {
            switch(count++) {
                case 0: return input.currentChar() == 'H';
                case 1: return input.currentChar() == 'T';
                case 2: return input.currentChar() == 'T';
                case 3: return input.currentChar() == 'P';
                case 4: return input.currentChar() == '/';
                case 5: return input.currentChar() == '1';
                case 6: return input.currentChar() == '.';
                case 7: char c = input.currentChar();
                        if (c == '1' || c == '0') {
                            setVersion(input);
                            finished = true;
                            return true;
                        }
                default: finished = true; return false;
            }
        }

        private void setVersion(Input input) {
            requestHandler.setHttpVersion(input.sofar().consume());
        }

        public boolean isFinished() {
            return finished;
        }

        public void reset() {
            finished = false;
            count = 0;
        }
    }

    class MatchToNewline implements Match {
        private int jumpto;
        private boolean finished;

        public MatchToNewline() {
            this(-1);
        }

        public MatchToNewline(int jumpto) {
            this.jumpto = jumpto;
        }

        public boolean matches(Input input) {
            if ((finished = input.currentChar() == '\n') && jumpto != -1) {
                jumpto(jumpto);
            }
            return true;
        }

        public boolean isFinished() {
            return finished;
        }

        public void reset() {
            finished = false;
        }
    }

    class FinishedOrHeader implements Match {
        private boolean finished;
        public boolean matches(Input input) {
            switch(input.currentChar()) {
                case '\n': jumpto(matchStream.length);  return finished = true;
                case '\r': return true;
                default: input.backOne(); input.sofar().consume(); return finished = true;
            }
        }

        public boolean isFinished() {
            return finished;
        }

        public void reset() {
            finished = false;
        }
    }

    class HeaderName implements Match {
        private boolean finished;
        public boolean matches(Input input) {
            switch(input.currentChar()) {
                case ':':  setHeaderName(input); finished = true; return true;
                default: return true;
            }
        }

        private void setHeaderName(Input input) {
            headerName = input.sofar().consume();
            headerName = headerName.substring(0, headerName.length()-1);
        }

        public boolean isFinished() {
            return finished;
        }

        public void reset() {
            finished = false;
        }
    }

    class HeaderValue implements Match {
        private boolean finished;
        private boolean lastCharNewline;
        public boolean matches(Input input) {
            switch(input.currentChar()) {
                case '\n':  if (lastCharNewline) {
                                input.backOne();
                                finished = true;
                                setHeader(input);
                                jumpto(12);
                                return true;
                            }
                             else {
                               return lastCharNewline = true;
                            }
                case ' ':
                case '\t': lastCharNewline = false;  return true;
                default: if (lastCharNewline) { //
                            input.backOne();
                            finished = true;
                            setHeader(input);
                            jumpto(12);
                            return true;
                         }
                         else {
                           return true;
                         }
            }
        }

        private void setHeader(Input input) {
            String headerValue = input.sofar().consume();
            headerValue =  headerValue.substring(0, headerValue.length()-1).trim();
            requestHandler.setHeader(headerName, headerValue);
        }


        public boolean isFinished() {
            return finished;
        }

        public void reset() {
            lastCharNewline = false;
            finished = false;
        }
    }
}

