package org.scott.ajserver.ajservlet;

import freemarker.template.*;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;

import org.scott.ajserver.AjServlet;
import org.scott.common.nio.http.HttpAjaxRequest;
import org.scott.common.nio.http.HttpAjaxResponse;


public class HelloAjServlet extends AjServlet {

    private Configuration cfg;

	public void setCfg(Configuration cfg) {
	  this.cfg = cfg;
	}
	
	protected void doAjax(HttpAjaxRequest request, HttpAjaxResponse response) throws IOException {
		response.setContentType("text/html");
        Writer writer = new OutputStreamWriter(response.getOutputStream());
        
		try {
		 Template temp = cfg.getTemplate("hello.ftl");
		 Map root = new HashMap();
		temp.process(root, writer);
		}
		catch(TemplateException  x){
		  throw  new IOException(x.getMessage());
		}
		writer.flush();
	    writer.close();
	}
	
}
