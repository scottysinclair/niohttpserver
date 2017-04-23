package org.scott.ajserver;

import java.io.IOException;

import org.scott.common.nio.http.HttpAjaxRequest;
import org.scott.common.nio.http.HttpAjaxResponse;

/**
 * Created by IntelliJ IDEA.
 * User: N006719
 * Date: 21-Jun-2006
 * Time: 23:04:22
 * To change this template use File | Settings | File Templates.
 */
public abstract class AjServlet {

    public void init(){}

    protected abstract void doAjax(HttpAjaxRequest request, HttpAjaxResponse response) throws IOException ;
}
