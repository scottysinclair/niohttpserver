package org.scott.common.statemachine;

/**
 * Created by IntelliJ IDEA.
 * User: N006719
 * Date: 19-Jul-2006
 * Time: 13:28:16
 * To change this template use File | Settings | File Templates.
 */
interface Buffer {
    public void clear();
    public String consume();
}
