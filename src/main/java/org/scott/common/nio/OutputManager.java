package org.scott.common.nio;

import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ClosedChannelException;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import org.scott.common.nio.events.ChannelEvent;
import org.scott.common.nio.events.DataTransferredEvent;
import org.scott.psbus.PubSubBus;

/**
 * handles writing data back to sockets using NIO
 * Manages a set of selector threads, each selector can have many channels registered with it.
 * so we could have 10 SelectorThreads managing 1000s of socket writes.
 * 
 */
public class OutputManager  {
	private static Logger LOG = Logger.getLogger(OutputManager.class.getName());
	
    private SelectorThread selectors[];
    private int nextThread;
    private PubSubBus bus;

    public OutputManager(int numberOfThreads, boolean daemon) throws IOException {
    	bus = PubSubBus.NULL_BUS;
        try {
            selectors = new SelectorThread[numberOfThreads];
            for (int i=0; i<selectors.length; i++) {
                selectors[i] = new SelectorThread("Socket Writer " + (i+1));
                selectors[i].setDaemon(daemon);
            }
        }
        catch(IOException x) {
            cleanupSelectors();
            throw x;
        }
    }
    
    public void setBus(PubSubBus bus) {
		this.bus = bus;
	}

	public void start() {
        for (int i=0; i<selectors.length; i++) {
          selectors[i].start();
        }
    }

    public void finishWhenAllIsWritten(boolean waitFor) throws InterruptedException {
        finishWhenAllIsWritten();
        if (waitFor) {
            waitUntilFinished();
        }
    }

    public void finishWhenAllIsWritten() {
        for (int i=0; i<selectors.length; i++) {
          selectors[i].finishWhenAllIsWritten();
        }
    }

    public void waitUntilFinished() throws InterruptedException {
        for (int i=0; i<selectors.length; i++) {
            selectors[i].join();
        }
    }

    public OutputSender registerChannel(SocketChannel channel) {
        OutputSender sender = new OutputSender(selectors[nextThread], channel);
        nextThread  = (nextThread + 1) % selectors.length;
        return sender;
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
     * output sender is called from outside the OutputManager to send bytes to the socket.
     * this class just queues a write request to the selector thread for this channel.
     * @author N006719
     *
     */
    public class OutputSender  {
        private SelectorThread selectorThread;
        private KeyWriter keyWriter;
        private SocketChannel channel;
        private SelectionKey key;

        public OutputSender(SelectorThread selectorThread, SocketChannel channel) {
            this.selectorThread = selectorThread;
            this.channel = channel;
            this.keyWriter = new KeyWriter(this);
        }
        
        public KeyWriter getKeyWriter() {
			return keyWriter;
		}

		public SocketChannel getChannel() {
			return channel;
		}

		public void setKey(SelectionKey key) {
            this.key = key;
        }

        public SelectionKey getKey() {
            return key;
        }
        
        public boolean channelStillOpen() throws IOException {
            if (!channel.isOpen()) {
                key.cancel();
                return false;
            }
            return true;
        }

		
		public void close() throws IOException {
			channel.close();
			key.cancel();
			bus.publishTopic(OutputManager.class, "close", new ChannelEvent(channel));
		}

        public void sendBytes(ByteBuffer buf) {
            selectorThread.addWriteRequest(new WriteRequest(this, buf));
        }
        public void sendBytes(ByteBuffer buf[], boolean keepAlive) {
            selectorThread.addWriteRequest(new WriteRequest(this, buf));
        }
    }

    /***
     * represents a set of bytes which should be sent to the socket.
     * @author N006719
     *
     */
    private class WriteRequest {
        private OutputSender outputSender;
        private ByteBuffer buf[];
        private long totalWritten = 0; 

        public WriteRequest(OutputSender outputSender, ByteBuffer buf) {
            this(outputSender, new ByteBuffer[]{buf});
        }

        public WriteRequest(OutputSender outputSender, ByteBuffer buf[]) {
            this.outputSender = outputSender;
            this.buf = buf;
        }

        public boolean writeOut() throws IOException {
        	SocketChannel channel = outputSender.getChannel();
            long count = channel.write(buf);
            totalWritten += count;
            while(count > 0) {
                count = channel.write(buf);
                totalWritten += count;
            }
            return allDataWritten();
        }
        
        public long getTotalWritten() {
			return totalWritten;
		}

		public SelectionKey getKey() {
            return outputSender.getKey();
        }

        private boolean allDataWritten(){
            boolean complete = true;
            for (int i=0; i<buf.length && complete; i++) {
                complete = !buf[i].hasRemaining();
            }
            return complete;
        }
    }


    /***
     * writes bytes to a single channel in the order received.
     * @author N006719
     *
     */
    public static class KeyWriter {
        private List<WriteRequest> queuedWrites;
        private OutputSender outputSender;

        public KeyWriter(OutputSender outputSender) {
            this.outputSender = outputSender;
            queuedWrites = new LinkedList<WriteRequest>();
        }
        
        public void close() throws IOException {
        	outputSender.close();
        }

        public void addWriteRequest(WriteRequest write) {
            queuedWrites.add(write);
//            LOG.info("added write request: size=" + queuedWrites.size());
        }

        public boolean channelStillOpen() throws IOException {
            return outputSender.channelStillOpen();
        }

        public int write() throws IOException  {
        	int totalCount = 0;
            for (Iterator<WriteRequest> i=queuedWrites.iterator(); i.hasNext();) {
                WriteRequest nextWriteRequest = (WriteRequest)i.next();
                boolean allDataWritten = nextWriteRequest.writeOut();
                totalCount += nextWriteRequest.getTotalWritten();
                if (allDataWritten) {
                    i.remove();
  //                  LOG.info("written write request: size=" + queuedWrites.size());
                    if (!i.hasNext()) {
                        if (true) {
                             //should re-register interest when a new write request comes in
                            nextWriteRequest.getKey().interestOps(0);
                        }
                        else {
                            close();
                        }
                    }
                }
            }
            return totalCount;
        }
    }

    /***
     * a thread which manages writing to an NIO selector
     * @author N006719
     *
     */
    private class SelectorThread extends Thread {
        private final List<WriteRequest> newWriteRequestsPending;
        private Selector selector;
        private boolean finishWhenAllIsWritten;
        private boolean pendingFinish;

        public SelectorThread(String name) throws IOException  {
            super(name);
            newWriteRequestsPending = new LinkedList<WriteRequest>();
            selector = Selector.open();
        }

        public synchronized void finishWhenAllIsWritten() {
            finishWhenAllIsWritten = true;
            if (selector.isOpen()) {
               selector.wakeup();
            }
        }

        public void addWriteRequest(WriteRequest write)  {
            synchronized(newWriteRequestsPending) {
                newWriteRequestsPending.add(write);
            }
            selector.wakeup();
        }

        public synchronized void setPendingFinish() {
            pendingFinish = true;
            selector.wakeup();
        }

        public void close() throws IOException {
            selector.close();
        }

       List<SelectionKey> selectedKeys = new ArrayList<SelectionKey>(20);
        public void run() {
            try {
                while(!pendingFinish) {
                    addNewChannelsPending();

                    selector.select();

                    if (finishWhenAllIsWritten && selector.keys().size() == 0) {
                        return;
                    }

                    //iterator across all  of the channels which are ready to be written to
                    //and write to them.
                    for (Iterator<SelectionKey> i=safeSelectedKeysIterator(selector); i.hasNext();) {
                        SelectionKey nextKey = (SelectionKey)i.next();
                        KeyWriter keyWriter = (KeyWriter)nextKey.attachment();
                        try {
                            if (nextKey.isValid() && nextKey.isWritable()) {
                                if (keyWriter.channelStillOpen()) {
                                   int totalWritten = keyWriter.write();
                                   bus.publishTopic(OutputManager.class, "write", new DataTransferredEvent(nextKey.channel(), totalWritten));
                                }
                            }
                        }
                        catch(ClosedChannelException x) {
                            x.printStackTrace(System.err);
                        }
                        catch(IOException x) {
                        	keyWriter.close();
                            System.err.println("IO Error writing packet");
                            x.printStackTrace(System.err);
                        }
                    }
                }
            }
            catch(IOException x) {
                System.err.println("IO Error on selector");
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

        private Iterator<SelectionKey> safeSelectedKeysIterator(Selector selector) {
            Set<SelectionKey> set = selector.selectedKeys();
            selectedKeys.clear();
            synchronized(set) {
               selectedKeys.addAll(set);
            }
            return selectedKeys.iterator();
        }

        /*** adds new channels to this thread's selector. */
        private void addNewChannelsPending() {
            synchronized(newWriteRequestsPending) {
                for (Iterator i=newWriteRequestsPending.iterator(); i.hasNext();) {
                    try {
                        WriteRequest nextWriteRequest = (WriteRequest)i.next();
                        SelectionKey key = nextWriteRequest.getKey();
                        if (key == null) {
                            key = nextWriteRequest.outputSender.getChannel().register(selector, SelectionKey.OP_WRITE);
                            nextWriteRequest.outputSender.setKey(key);
                            nextWriteRequest.outputSender.getKeyWriter().addWriteRequest(nextWriteRequest);
                            key.attach(nextWriteRequest.outputSender.getKeyWriter());
                        }
                        else {
                            if ((key.interestOps() & SelectionKey.OP_WRITE) == 0) {
                                key.interestOps(SelectionKey.OP_WRITE);
                            }
                            KeyWriter writer = (KeyWriter)key.attachment();
                            writer.addWriteRequest(nextWriteRequest);
                        }
                    }
                    catch(ClosedChannelException x) {
                        System.err.println("Could not register channel with selector, channel closed.");
                    }
                    catch(IOException x) {
                        System.err.println("Could not register channel with selector");
                        x.printStackTrace(System.err);
                    }
                }
                newWriteRequestsPending.clear();
            }
        }
    }
}
