package org.scott.common.nio.http;

import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;

import org.scott.ajserver.AjaxProcessor;
import org.scott.common.nio.InputConsumer;
import org.scott.common.statemachine.HttpStateMachine;
import org.scott.common.statemachine.Input;

import java.io.IOException;

/**
 * parses an http request 
 */
public class HttpRequestParser implements InputConsumer {
    private boolean receivedFullRequest;
    private HttpStateMachine http;
    private HttpAjaxRequest httpRequest;
    private AjaxProcessor ajaxProcessor;
    private Input input;

    public HttpRequestParser(AjaxProcessor ajaxProcessor) {
    	this.ajaxProcessor = ajaxProcessor;
    	init();
    }
    
	public void consumeInput(SelectionKey key, CharBuffer data) throws IOException  {
		if (receivedFullRequest) {
			receivedFullRequest = false;
			init();		
		}
        input.newInput(data.array(), data.arrayOffset(), data.limit());
        if (!http.matches(input)) {
            throw new IOException("Bad HTTP Request");
        }
        if (http.isFinished()) {
            receivedFullRequest = true;
            ajaxProcessor.processRequestAsynch(key, httpRequest);
        }
    }
	
	private void init() {
        http = new HttpStateMachine();
        httpRequest = new HttpAjaxRequest();
        http.setRequestHandler(httpRequest);
        input = new Input();
	}

    public boolean receivedFullRequest() {
        return receivedFullRequest;
    }

    public HttpAjaxRequest getHttpRequest() throws IOException  {
        return httpRequest;
    }

    public void clear() {
        http.reset();
    }


}
