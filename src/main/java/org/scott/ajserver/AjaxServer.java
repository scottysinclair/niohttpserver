package org.scott.ajserver;

import freemarker.template.*;

import java.io.*;
import java.util.logging.Logger;

import org.scott.ajserver.ajservlet.HelloAjServlet;
import org.scott.ajserver.ajservlet.StatsAjServlet;
import org.scott.common.nio.ConnectManager;
import org.scott.common.nio.InputManager;
import org.scott.common.nio.OutputManager;
import org.scott.common.stats.ChartBuilder;
import org.scott.common.threading.ThreadPool;
import org.scott.psbus.PubSubBus;
import org.scott.psbus.PublicationData;
import org.scott.psbus.Topic;
import org.scott.psbus.TopicListener;


/**
main class which starts the server
 */
public class AjaxServer {

    private static final Logger LOG = Logger.getLogger(Topic.class.getName());

    private ConnectManager connectManager;

    private OutputManager outputManager;

    private InputManager inputManager;

    private ThreadPool httpRequests;

    private AjaxProcessor ajProcessor;

    private PubSubBus bus;

    private int numberOfOutputThreads = 1;
    private int numberOfInputThreads = 1;
    private int numberOfAJServletThreads = 1;

    public AjaxServer() {
        ajProcessor = new AjaxProcessor();
        bus = PubSubBus.NULL_BUS;
    }


    public void setNumberOfOutputThreads(int numberOfOutputThreads) {
        this.numberOfOutputThreads = numberOfOutputThreads;
    }

    public void setNumberOfInputThreads(int numberOfInputThreads) {
        this.numberOfInputThreads = numberOfInputThreads;
    }

    public void setNumberOfAJServletThreads(int numberOfAJServletThreads) {
        this.numberOfAJServletThreads = numberOfAJServletThreads;
    }

    public void setBus(PubSubBus bus) {
        this.bus = bus;
    }

    public void startListening(int port, boolean daemon) throws IOException {
        InputHandler inputHandler = new InputHandler();

        connectManager = new ConnectManager(port);
        connectManager.setConnectionListener(inputHandler);
        connectManager.setBus(bus);

        inputManager = new InputManager(numberOfInputThreads, daemon);
        inputManager.setInputConsumer(inputHandler);
        inputManager.setBus(bus);

        outputManager = new OutputManager(numberOfOutputThreads, daemon);
        outputManager.setBus(bus);

        httpRequests = new ThreadPool();

        ajProcessor.setThreadPool(httpRequests);
        ajProcessor.setBus(bus);
        inputHandler.setInputManager(inputManager);
        inputHandler.setOutputManager(outputManager);
        inputHandler.setAjaxProcessor(ajProcessor);

        httpRequests.start("http-request-processing", numberOfAJServletThreads, daemon);
        outputManager.start();
        inputManager.start();
        connectManager.startListening(daemon);
    }

    public void addAjServlet(String path, AjServlet ajServlet) {
        ajProcessor.addAjServlet(path, ajServlet);
        ajServlet.init();
    }

    public void waitUntilShutdown() throws InterruptedException {
        connectManager.waitUntilFinished();
    }

    public void shutdown() throws IOException {
        try {
            httpRequests.shutdown();
        }catch(InterruptedException  x) {}
        try { inputManager.shutdown();
        }catch(InterruptedException  x) {}
        try { outputManager.finishWhenAllIsWritten(true);
        }catch(InterruptedException x) {}
        try { connectManager.stopListening(); }
        catch(InterruptedException x){}
    }

    public static void main(String args[]) {
        AjaxServer server = new AjaxServer();

     try {
    Configuration cfg = new Configuration();
        cfg.setDirectoryForTemplateLoading(
                new File("templates"));
        cfg.setObjectWrapper(new DefaultObjectWrapper());



        PubSubBus bus = new PubSubBus();
        ChartBuilder chartBuilder = new ChartBuilder(bus);
        chartBuilder.subscribe("org.scott.common.nio.**");

        bus.subscribeTopic("org.scott.common.nio.**", new TopicListener() {
            public void topicPublished(PublicationData data) {
    //			LOG.info(data.getTopic());
            }
        });

        server.setNumberOfInputThreads(5);
        server.setNumberOfOutputThreads(5);
        server.setNumberOfAJServletThreads(5);


        server.setBus(bus);
        HelloAjServlet hw = new HelloAjServlet();
        hw.setCfg(cfg);
        server.addAjServlet("/", hw);

        StatsAjServlet ss = new StatsAjServlet(chartBuilder);
        ss.setCfg(cfg);
        server.addAjServlet("/stats", ss);

        int port  = 8080;
        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        }
        server.startListening(port, false);
        System.out.println("listening on " + port);
//            Thread.sleep(1000);
//            server.shutdown();
        }
        catch(Exception x) {
            x.printStackTrace(System.err);
            try { server.shutdown(); }
            catch(Exception x2) {
                x2.printStackTrace(System.err);
            }
        }
    }
}
