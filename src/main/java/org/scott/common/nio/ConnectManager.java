package org.scott.common.nio;

import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.scott.psbus.PubSubBus;

import java.net.InetSocketAddress;
import java.io.IOException;

/**
 * Uses NIO to accept new socket connections and then delegates to the specified input handler.
 */
public class ConnectManager implements Runnable {
	
	public interface ConnectionListener {
		public void connectionReceived(SocketChannel channel);
	}
	
    //private Selector selector;
    private ServerSocketChannel serverChannel;
    private Thread serverSocketThread;
    private ConnectionListener connectionListener;
    private PubSubBus bus;

    public ConnectManager(int port) throws IOException {
       getSelector(port);
        bus = PubSubBus.NULL_BUS;
    }
    
    public void setBus(PubSubBus bus) {
		this.bus = bus;
	}

	public void setConnectionListener(ConnectionListener inputHandler) {
        this.connectionListener = inputHandler;
    }

    public void startListening(boolean daemon)  {
        Thread t = new Thread(this, "ajax-socket-thread");
        t.setDaemon(daemon);
        t.start();
    }

    public void run() {
        try {
            listen();
        }
        catch(IOException x) {
            x.printStackTrace(System.err);
        }
    }
    
    public void listen() throws IOException {
    	while(true) {
    		connectionListener.connectionReceived(serverChannel.accept());
    	}
    }
/*
    public void listen() throws IOException {
        serverSocketThread = Thread.currentThread();
        Set<SelectionKey> selectedKeys = new HashSet<SelectionKey>();
        while(true) {
            selectedKeys.clear();
      //      selector.select();
            if (!serverChannel.isOpen()) {
                return;
            }
                        
            synchronized(selector) {
              selectedKeys.addAll(selector.selectedKeys());
            }
            long start = System.currentTimeMillis();
            Iterator<SelectionKey> it = selectedKeys.iterator();
            while (it.hasNext()) {
                SelectionKey nextKey = (SelectionKey) it.next();
                try {
                    if (nextKey.isValid() && nextKey.isAcceptable()) {
                        connectionListener.connectionReceived(nextKey);
                    }
                }
                catch(CancelledKeyException x) {
                   x.printStackTrace(System.err);
                }
                 it.remove();
            }
            long end = System.currentTimeMillis();
            System.out.println("conn " + (end-start));
        }
    }
*/
    public void waitUntilFinished() throws InterruptedException {
        while(serverSocketThread == null) {
            Thread.sleep(1000);
        }
        if (serverSocketThread != Thread.currentThread()) {
            serverSocketThread.join();
        }
    }

    public void stopListening() throws IOException, InterruptedException  {
        serverChannel.close();
        if (serverSocketThread != Thread.currentThread()) {
            serverSocketThread.join();
        }
    }

    private Selector getSelector(int port) throws IOException {
        serverChannel = ServerSocketChannel.open();
      //  Selector selector = Selector.open();
        serverChannel.socket().bind(new InetSocketAddress(port));
        serverChannel.configureBlocking(true);
        //serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        return null;
    }

}
