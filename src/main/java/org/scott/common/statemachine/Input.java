package org.scott.common.statemachine;

/**
 * Created by IntelliJ IDEA.
 * User: N006719
 * Date: 13-Jul-2006
 * Time: 13:31:48
 * To change this template use File | Settings | File Templates.
 */
public class Input {

    private MyBuffer buffer = new MyBuffer();
    private StringBuffer sb;
    private int position;

    public Input() {
        this.sb = new StringBuffer();
        position = 0;
    }

    public void newInput(String inputData) {
        sb.append(inputData);
    }

    public void newInput(char inputData[]) {
        sb.append(inputData);
    }
    public void newInput(char inputData[], int offset, int length) {
        sb.append(inputData, offset, length);
    }

    public void backOne() {
        position--;
        buffer.backOne();
    }

    public Buffer sofar() {
        return buffer;
    }

    public final char currentChar() {
        return sb.charAt(position++);
    }

    public int getPosition() {
        return position;
    }

    public void gotoPosition(int pos) {
        position = pos;
    }

    public boolean hasMore() {
        return position < sb.length();
    }

    class MyBuffer implements Buffer {
        private int bufferPos = 0;
        public void clear() {
            bufferPos = position;
        }

        public void setPos(int pos) {
            bufferPos = pos;

        }

        public void backOne() {
            if (bufferPos > 0) {
                bufferPos--;
            }
        }


        public String consume() {
            String s = sb.substring(bufferPos, position);
            bufferPos = position;
            return s;
        }
    }
}
