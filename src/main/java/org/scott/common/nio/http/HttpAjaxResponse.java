package org.scott.common.nio.http;

import java.nio.channels.SelectionKey;
import java.nio.ByteBuffer;
import java.io.*;
import java.text.SimpleDateFormat;

import org.scott.common.nio.KeyInfo;
import org.scott.common.nio.OutputManager;

/**
 * Created by IntelliJ IDEA.
 * User: N006719
 * Date: 21-Jun-2006
 * Time: 11:42:00
 * To change this template use File | Settings | File Templates.
 */
public class HttpAjaxResponse {
    private static SimpleDateFormat rfc1123DateFormat = new SimpleDateFormat("EEE, d MMM y k:m:s 'GMT'");

    private OutputManager.OutputSender outputSender;
    private ByteArrayOutputStream content;
    private byte contentData[];
    private String contentType;
    private boolean notFound;

    public HttpAjaxResponse(SelectionKey key) {
        this.outputSender = ((KeyInfo)key.attachment()).getOutputSender();
        content = new ByteArrayOutputStream() {
            public void close() throws IOException {
                sendResponse();
            }
        };
    }

    public void setNotFound(boolean notFound) {
        this.notFound = notFound;
    }

    private String buildResponse() {
        contentData = content.toByteArray();
        StringBuffer buffer = new StringBuffer();
        if (notFound) {
            buffer.append("HTTP/1.0 404 Not Found\n");
        }
        else {

            buffer.append("HTTP/1.1 200 OK\n");
//            buffer.append("Date: ");
 //           buffer.append(rfc1123DateFormat.format(new Date()));
            buffer.append("Date: Fri, 31 Dec 1999 23:59:59 GMT\n");
//            buffer.append("connection: close\n");
            buffer.append("Content-Type: ");
            buffer.append(contentType);
            buffer.append("\n");
            buffer.append("Content-Length: ");
            buffer.append(contentData.length);
            buffer.append("\n\n");
        }
        return buffer.toString();
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public OutputStream getOutputStream() {
        return content;
    }

    public void sendResponse() throws IOException {
        ByteBuffer buf[] = new ByteBuffer[2];
        buf[0] = ByteBuffer.wrap(buildResponse().getBytes());
        buf[1] = ByteBuffer.wrap(contentData);
        outputSender.sendBytes(buf, false);
    }
}
