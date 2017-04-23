package org.scott.common.nio.http;

import java.util.Properties;

import org.scott.common.statemachine.HttpRequestParserHandler;

import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: N006719
 * Date: 21-Jun-2006
 * Time: 11:41:53
 * To change this template use File | Settings | File Templates.
 */
public class HttpAjaxRequest implements HttpRequestParserHandler {
    private String method;
    private String path;
    private String version;
    private Properties queryParam;
    private Map header;

    public HttpAjaxRequest() {
        queryParam = new Properties();
        header = new HashMap();
    }

    public Iterator parameterNames() {
        return queryParam.keySet().iterator();
    }

    public String getQueryParameter(String name) {
        return queryParam.getProperty(name);
    }

    public String getFullPath() {
        return path;
    }

    public String getHttpVersion() {
        return version;
    }

    public String getMethod() {
        return method;
    }

    public String[] getHeaderValue(String headerName) {
        return (String[])header.get(headerName);
    }

    public Iterator headerNames() {
        return header.keySet().iterator();
    }

    ///--------- implementing settings for state machine side

    public void setMethod(String method) {
        this.method = method;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setQueryParam(String name, String value) {
        queryParam.setProperty(name, value);
    }

    public void setHttpVersion(String version) {
        this.version = version;
    }

    public void setHeader(String name, String value) {
        header.put(name, value);
    }
}
