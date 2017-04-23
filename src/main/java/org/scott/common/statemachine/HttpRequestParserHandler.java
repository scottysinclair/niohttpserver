package org.scott.common.statemachine;

/**
 * Created by IntelliJ IDEA.
 * User: N006719
 * Date: 12-Jul-2006
 * Time: 16:31:55
 * To change this template use File | Settings | File Templates.
 */
public interface HttpRequestParserHandler {
    public void setMethod(String method);
    public void setPath(String path);
    public void setQueryParam(String name, String value);
    public void setHttpVersion(String version);
    public void setHeader(String name, String value);
}
