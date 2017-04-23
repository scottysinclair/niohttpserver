package org.scott.common.threading;

/**
 * Created by IntelliJ IDEA.
 * User: N006719
 * Date: 23-Jun-2006
 * Time: 16:49:22
 * To change this template use File | Settings | File Templates.
 */
public interface FutureValue {
    public void setValue(Object _object);

    public Object getValue();
}
