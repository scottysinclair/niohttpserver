package org.scott.ajserver.ajservlet;

import freemarker.template.*;
import org.jfree.chart.ChartUtilities;
import org.scott.ajserver.AjServlet;
import org.scott.common.nio.http.HttpAjaxRequest;
import org.scott.common.nio.http.HttpAjaxResponse;
import org.scott.common.stats.ChartBuilder;

import java.util.*;
import java.io.*;
import java.awt.image.BufferedImage;

/**
 * Ajax servlet for showing connection statistics
 */
public class StatsAjServlet extends AjServlet {
    private Map images = Collections.synchronizedMap(new HashMap());

    private ChartBuilder charBuilder;

    public StatsAjServlet(ChartBuilder charBuilder) {
        this.charBuilder = charBuilder;
    }

    private Configuration cfg;

    public void setCfg(Configuration cfg) {
      this.cfg = cfg;
    }

    private void writeHeader(Writer writer) throws IOException {
        writer.write("<html><head>");
        writer.write("<META HTTP-EQUIV=\"Pragma\" CONTENT=\"no-cache\">");
        writer.write("<META HTTP-EQUIV=\"Expires\" CONTENT=\"-1\">");
        writer.write("<title>Statistics</title></head>");
        writer.write("<body>");
        writer.write("<H1>NIO HTTP Server Statistics</H1>");
        writer.write("<hr size=\"1\"/>");
    }

    private void finishPage(Writer writer) throws IOException {
        writer.write("</body>");
        writer.write("</html>");
        writer.flush();
        writer.close();
    }

    private void writeImageLinks(Writer writer, Map images) throws IOException {
        for (Iterator i=images.keySet().iterator(); i.hasNext();) {
            String nextImageKey = (String)i.next();
            writer.write("<img src=\"stats/" + nextImageKey + "\" width=\"400\" height=\"300\"/><br/>\n");
            writer.write(nextImageKey + "<br/>\n");
            writer.write("<br/>\n");
        }

    }

    protected void doAjax(HttpAjaxRequest request, HttpAjaxResponse response) throws IOException {
        if (request.getFullPath().endsWith("stats")) {
            images.clear();
            response.setContentType("text/html");
            try ( Writer writer = new OutputStreamWriter(response.getOutputStream()) ) {
                 Template temp = cfg.getTemplate("stats/index.ftl");
                 Map root = new HashMap();
                 temp.process(root, writer);
                }
                catch(TemplateException  x){
                  throw  new IOException(x.getMessage());
                }
//            writeHeader(writer);
//            writer.write("Click <a href=\"stats/mem\">here</a> to view memory stats.<br/>");
//            writer.write("Click <a href=\"stats/io\">here</a> to view io stats.<br/>");
//            finishPage(writer);
         }
        else if (request.getFullPath().endsWith("mem")) {
         }
        else if (request.getFullPath().endsWith("io")) {

          if ("1".equals(request.getQueryParameter("clear"))) {
              charBuilder.clearChannels();
          }
          Map root = new HashMap();
          root.put("topics", charBuilder.getTopics());
          root.put("channels", charBuilder.getChannels());
          writeOut(request, response, "stats/io.ftl", root);
         }
         else {
            response.setContentType("image/png");
            OutputStream output = response.getOutputStream();
            int i = request.getFullPath().lastIndexOf('/');
            String pic = request.getFullPath().substring(i+1, request.getFullPath().length());
            BufferedImage image = charBuilder.chart(pic, 400, 300);
            if (image != null) {
//                ByteArrayOutputStream imageOutput = new ByteArrayOutputStream();
//                ImageIO.write(image, "jpeg", imageOutput);
//                output.write(imageOutput.toByteArray());
                output.write( ChartUtilities.encodeAsPNG(image) );
            }
            else {
                System.out.println("no image " + pic + " found");
                System.out.println("full path info = " + request.getFullPath());
            }
            output.flush();
            output.close();
        }
    }

    private void writeOut(HttpAjaxRequest request, HttpAjaxResponse response, String template, Map root) throws IOException {
        response.setContentType("text/html");
        Writer writer = new OutputStreamWriter(response.getOutputStream());

        try {
         Template temp = cfg.getTemplate( template );
        temp.process(root, writer);
        }
        catch(TemplateException  x){
          throw  new IOException(x.getMessage());
        }
        writer.flush();
        writer.close();
    }
}
