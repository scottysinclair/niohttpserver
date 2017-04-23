package org.scott.ajserver;

import java.nio.channels.SelectionKey;
import java.util.Map;

import org.scott.common.nio.http.HttpAjaxRequest;
import org.scott.common.nio.http.HttpAjaxResponse;
import org.scott.common.threading.ThreadPool;
import org.scott.psbus.PubSubBus;

import java.util.HashMap;
import java.io.Writer;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * processes http requests by delegating to a regsistered AjSerlvet. 
 */
public class AjaxProcessor  {
    private ThreadPool threadPool;
    private PubSubBus bus;
    private Map ajaxlets;

    public AjaxProcessor() {
        ajaxlets = new HashMap();
        bus = PubSubBus.NULL_BUS;
    }

    public void setBus(PubSubBus bus) {
		this.bus = bus;
	}

	public void setThreadPool(ThreadPool threadPool) {
        this.threadPool = threadPool;
    }

     public void addAjServlet(String path, AjServlet ajServlet) {
        ajaxlets.put(path, ajServlet);
    }

    public void processRequestAsynch(SelectionKey key, HttpAjaxRequest httpRequest) {
        threadPool.queue(new RequestRunner(key, httpRequest));
    }

    public void processRequest(SelectionKey key, HttpAjaxRequest req) {
        HttpAjaxResponse res = new HttpAjaxResponse(key);
        try {
            AjServlet ajServlet = getAjServlet(req);
            if (ajServlet != null) {
            	bus.publishTopic(AjaxProcessor.class, "beforeDoAjax",key.channel());
                ajServlet.doAjax(req, res);
            	bus.publishTopic(AjaxProcessor.class, "afterDoAjax",key.channel());
            }
            else {
                notFound(req, res);
            }
        }
        catch(IOException x) {
            x.printStackTrace(System.err);
        }
    }

    private class RequestRunner implements Runnable {
        private SelectionKey key;
        private HttpAjaxRequest httpRequest;

        public RequestRunner(SelectionKey key, HttpAjaxRequest httpRequest) {
            this.key = key;
            this.httpRequest = httpRequest;
        }

        public void run() {
            processRequest(key, httpRequest);
        }
    }

    private AjServlet getAjServlet(HttpAjaxRequest req){
        return (AjServlet)ajaxlets.get(getServletPart(req.getFullPath()));
    }

    private String getServletPart(String path) {
        int i = path.indexOf('/', 1);
        return i == -1 ? path : path.substring(0, i);
    }

    private void notFound(HttpAjaxRequest req, HttpAjaxResponse res) throws IOException {
        res.setContentType("text/html");
//        res.setNotFound(true);
//        res.sendResponse();
        Writer out = new OutputStreamWriter(res.getOutputStream());
        out.write("<html><head><title>Not Found</title></head><body>Not Found<br/>");
        out.write("</body></html>");
        out.flush();
        out.close();
    }

    private void couldNotBuildRequest(HttpAjaxResponse res, Exception x) throws IOException {
        res.setContentType("text/html");
        Writer out = new OutputStreamWriter(res.getOutputStream());
        out.write("<html><head><title>Could not parse request</title></head><body>Could not parse request<br/>Reason:<pre>");
        PrintWriter pw = new PrintWriter(out);
        x.printStackTrace(pw);
        pw.flush();
        out.write("</pre></body></html>");
        out.flush();
        out.close();
    }

}
