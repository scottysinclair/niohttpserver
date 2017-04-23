package org.scott.common.threading;

/**
 * a pool of threas which executes Runnables on a FIFO babsis
 */
public class ThreadPool {
    private WorkingThread threads[];
    private BlockingFIFOQueue queue;

    public interface ErrorHandler  {
        public void handleError(Runnable run, Throwable t);
    }

    private class RunErrPair {
        public final Runnable runnable;
        public final ErrorHandler errorHandler;

        public RunErrPair(Runnable run, ErrorHandler errorHandler) {
            this.runnable = run;
            this.errorHandler = errorHandler;
        }
    }

    public ThreadPool() {
        queue = new BlockingFIFOQueue();
    }

    public void queue(Runnable run) {
        queue(run, null);
    }

    public void queue(Runnable run, ErrorHandler errH) {
        queue.queue(new RunErrPair(run, errH));
    }

    public void start(String threadName, int numberOfThreads, boolean daemon) {
        threads = new WorkingThread[numberOfThreads];
        for (int i=0; i<threads.length; i++) {
            threads[i] = new WorkingThread(threadName + " " + (i+1));
            threads[i].setDaemon(daemon);
            threads[i].start();
        }
    }

    private void shutdownThreads() {
        for (int i=0; i<threads.length; i++) {
            threads[i].setPendingFinish();
            threads[i].interrupt();
        }
    }

    public void shutdown() throws InterruptedException {
        shutdownThreads();
        for (int i=0; i<threads.length; i++) {
            threads[i].join();
        }
    }

    /***
     * a thread which executes runnables.
     * @author N006719
     *
     */
    private class WorkingThread extends Thread {
        private boolean pendingFinish;

        public WorkingThread(String name) {
            super(name);
        }

        public synchronized void setPendingFinish() {
            this.pendingFinish = true;
        }

        public void run() {
            while(!pendingFinish) {
                RunErrPair re = null;
                try {
                    re = (RunErrPair)queue.nextItem();
                    re.runnable.run();
                }
                catch(InterruptedException x) {
                    System.err.println("Working thread interrupted!!!");
                }
                catch(RuntimeException x) {
                    if (re.errorHandler == null) {
                        System.err.println("Runtime exception occurred in Task Thread");
                        x.printStackTrace(System.err);
                        shutdownThreads();
                    }
                    else {
                        re.errorHandler.handleError(re.runnable, x);
                    }
                }
            }
        }
    }

}
