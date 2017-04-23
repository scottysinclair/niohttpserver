package org.scott.common.nio;

import java.nio.channels.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.Charset;
import java.io.IOException;
import java.util.*;

import org.scott.common.nio.events.ChannelEvent;
import org.scott.common.nio.events.DataTransferredEvent;
import org.scott.psbus.PubSubBus;

/**
 * manages a set of threads which read data from a set of selectors.
 * the input manager can read from 1000s of sockets using only 10 threads for example. 
 */
public class InputManager {
    private InputConsumer inputConsumer;
    private CharsetDecoder decoder;
    private SelectorThread selectors[];
    private int nextThread;
    private PubSubBus bus;

    public InputManager(int numberOfThreads, boolean daemon) throws IOException {
        Charset charset = Charset.forName("ISO-8859-1");
        decoder = charset.newDecoder();
        bus = PubSubBus.NULL_BUS;
        try {
            selectors = new SelectorThread[numberOfThreads];
            for (int i=0; i<selectors.length; i++) {
                selectors[i] = new SelectorThread("Socket Reader " + (i+1));
                selectors[i].setDaemon(daemon);
            }
            nextThread = 0;
        }
        catch(IOException x) {
            cleanupSelectors();
            throw x;
        }
    }

    public void setBus(PubSubBus bus) {
		this.bus = bus;
	}

	public void setInputConsumer(InputConsumer inputHandler) {
        this.inputConsumer = inputHandler;
    }

    public void start() {
        for (int i=0; i<selectors.length; i++) {
          selectors[i].start();
        }
    }

    public void addChannel(SocketChannel channel, Object attach) {
        selectors[nextThread].addChannel(channel, attach);
        nextThread = (nextThread + 1) % selectors.length;
		bus.publishTopic(InputManager.class, "addChannel", new ChannelEvent(channel));
    }

    public void shutdown() throws InterruptedException  {
        for (int i=0; i<selectors.length; i++) {
            selectors[i].setPendingFinish();
        }
        waitUntilFinished();
    }

    public void waitUntilFinished() throws InterruptedException {
        for (int i=0; i<selectors.length; i++) {
            selectors[i].join();
        }
    }

    private void cleanupSelectors() {
        for (int i=0; i<selectors.length; i++) {
            if (selectors[i] != null) {
                try { selectors[i].close(); }
                catch(IOException x) { x.printStackTrace(System.err);  }
            }
        }
    }

    /***
     * thread which reads data from any channels associated with
     * a selector, one instance could read data from 100s of channels.
     * @author N006719
     *
     */
    private class SelectorThread extends Thread {
        private final List<ChannelAttachmentPair> newChannelsPending;
        private ByteBuffer byteBufer = ByteBuffer.allocateDirect(1024);
        private CharBuffer charBuffer = CharBuffer.wrap(new char[1024]);
        private Selector selector;
        private boolean pendingFinish;

        public SelectorThread(String name) throws IOException {
            super(name);
            newChannelsPending = new LinkedList<ChannelAttachmentPair>();
            selector =  Selector.open();
        }

        public void addChannel(SocketChannel channel, Object attach) {
            synchronized(newChannelsPending) {
                newChannelsPending.add(new ChannelAttachmentPair(channel, attach));
            }
            selector.wakeup();
        }

        public synchronized void setPendingFinish() {
            pendingFinish = true;
            if (selector.isOpen()) {
                selector.wakeup();
            }
        }

        public void close() throws IOException {
            selector.close();
        }

        List<SelectionKey> selectedKeys = new ArrayList<SelectionKey>(20);
        /***
         * waits for data from a channel registered with the selector
         * and then reads from it.
         */
        public void run() {
            SelectionKey nextKey = null;
            try {
                while(!pendingFinish) {

                    addNewChannelsPending();

                    selector.select();

                    for (Iterator<SelectionKey> i=safeSelectedKeysIterator(selector); i.hasNext();) {
                        try {
                            nextKey = (SelectionKey)i.next();
                            if (nextKey.isValid() && nextKey.isReadable()) {
                                int numBytes = readFromSocket(nextKey);
                            	bus.publishTopic(InputManager.class, "read", new DataTransferredEvent(nextKey.channel(), numBytes));
                            }
                        }
                        catch(CancelledKeyException x) {
                            if (nextKey != null){
                                //happens on isReadable()
                                System.err.println("Cancelled key for host " + ((SocketChannel)nextKey.channel()).socket().getRemoteSocketAddress());
                                x.printStackTrace(System.err);
                            }
                        }
                    }


                }
             }
            catch(IOException x) {
                System.err.println(getName() + ": IO Error on selector");
                x.printStackTrace(System.err);
            }
            finally {
                if (selector.isOpen()) {
                    try { selector.close(); }
                    catch(IOException x2) {
                        x2.printStackTrace(System.err);
                    }
                }
            }
        }

        private class ChannelAttachmentPair {
            public final SocketChannel channel;
            public final Object object;

            public ChannelAttachmentPair(SocketChannel channel, Object object) {
                this.channel = channel;
                this.object = object;
            }
        }

        private void addNewChannelsPending() {
            synchronized(newChannelsPending) {
                for (Iterator<ChannelAttachmentPair> i=newChannelsPending.iterator(); i.hasNext();) {
                    ChannelAttachmentPair nextPair = (ChannelAttachmentPair)i.next();
                    try {
                        SelectionKey readKey = nextPair.channel.register(selector, SelectionKey.OP_READ);
                        if (nextPair.object != null) {
                            readKey.attach(nextPair.object);
                        }
                    }
                    catch(ClosedChannelException x) {
                        System.err.println(getName() + ": Could not register channel with selector, connection closed " + nextPair.channel.socket().getRemoteSocketAddress());
                    }
                    catch(IOException x) {
                        System.err.println(getName() + ": Could not register channel with selector");
                        x.printStackTrace(System.err);
                    }
                }
                newChannelsPending.clear();
            }
        }

        /***
         * read from the socket and give the data to the inputHandler.
         * @param key
         */
        private int readFromSocket(SelectionKey key) {
            int totalCount = 0;
        	SocketChannel socketChannel = (SocketChannel)key.channel();
            try {
                if (socketChannel.isOpen()) {
                	int count;
                    while((count = socketChannel.read(byteBufer)) > 0) {
                        byteBufer.flip();
                        decoder.decode(byteBufer, charBuffer, false);
                        charBuffer.flip();
                        inputConsumer.consumeInput(key, charBuffer);
                        charBuffer.clear();
                        byteBufer.clear();
                        totalCount += count;
                    }
                    if (count == -1) {
//                        socketChannel.close();
                        key.cancel();
                    }
                }
                else {
                    key.cancel();
                }
            }
            catch(IOException x) {
                System.err.println(getName() + ": IO error reading from host '" + socketChannel.socket().getRemoteSocketAddress() + "' " + x);
                x.printStackTrace(System.err);
                if (socketChannel.isOpen()) {
                   try { socketChannel.close(); } catch(IOException x2) { x2.printStackTrace(System.err); }
                }
                key.cancel();
                //try { selector.selectNow(); } catch(IOException x2) { x2.printStackTrace(System.err); }
            }
            return totalCount;
        }

        private Iterator<SelectionKey> safeSelectedKeysIterator(Selector selector) {
            Set<SelectionKey> set = selector.selectedKeys();
            selectedKeys.clear();
            synchronized(set) {
               selectedKeys.addAll(set);
            }
            return selectedKeys.iterator();
        }

    }
}
