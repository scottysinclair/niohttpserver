package org.scott.common.threading;

import java.util.LinkedList;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: N006719
 * Date: 21-Jun-2006
 * Time: 11:39:35
 * To change this template use File | Settings | File Templates.
 */
public class BlockingFIFOQueue {
    public interface QueuePruner {
        public boolean finished();
        public boolean prune(Object object);
    }

    protected final LinkedList internalQueue;

    public BlockingFIFOQueue() {
        internalQueue = new LinkedList();
    }

    public synchronized Object nextItem() throws InterruptedException {
        while(internalQueue.isEmpty()) {
            wait();
        }
        return internalQueue.removeFirst();
    }

    public Iterator iterator() {
        return internalQueue.iterator();
    }

    public synchronized int size() {
        return internalQueue.size();
    }

    public synchronized void queue(Object object) {
        if (object != null) {
            internalQueue.add(object);
            notify();
        }
    }

    public synchronized void pruneFromHead(QueuePruner prunner) {
        for (Iterator i=internalQueue.iterator(); i.hasNext() && !prunner.finished();) {
            if (prunner.prune(i.next())) {
                i.remove();
            }
        }
    }
}
