package org.scott.common.threading;

/*
  no synchronization, we don't care if we sleep too much
*/
public class NoSynchFutureValue implements FutureValue {
    private Object object;
    private Runnable tryAndGet;
    private int checkEvery;

    public NoSynchFutureValue(int checkEvery, Runnable tryAndGet) {
        this.checkEvery = checkEvery;
        this.tryAndGet = tryAndGet;
    }

    public void setValue(Object _object) {
        object = _object;
    }

    public Object getValue() {
        sleepFor();
        return object;
    }

    private void sleepFor() {
        while(object == null) {
            try {
                if (tryAndGet != null) tryAndGet.run();
//                System.out.println("wakeup");
                Thread.sleep(checkEvery);
            }
            catch(InterruptedException x) {}
        }
    }

}
