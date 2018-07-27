package org.apache.batik.util;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.ref.PhantomReference;
public class CleanerThread extends Thread {
    static volatile ReferenceQueue queue = null;
    static CleanerThread  thread = null;
    public static ReferenceQueue getReferenceQueue() {
        if ( queue == null ) {
            synchronized (CleanerThread.class) {
                queue = new ReferenceQueue();
                thread = new CleanerThread();
            }
        }
        return queue;
    }
    public static interface ReferenceCleared {
        void cleared();
    }
    public abstract static class SoftReferenceCleared extends SoftReference
      implements ReferenceCleared {
        public SoftReferenceCleared(Object o) {
            super (o, CleanerThread.getReferenceQueue());
        }
    }
    public abstract static class WeakReferenceCleared extends WeakReference
      implements ReferenceCleared {
        public WeakReferenceCleared(Object o) {
            super (o, CleanerThread.getReferenceQueue());
        }
    }
    public abstract static class PhantomReferenceCleared
        extends PhantomReference
        implements ReferenceCleared {
        public PhantomReferenceCleared(Object o) {
            super (o, CleanerThread.getReferenceQueue());
        }
    }
    protected CleanerThread() {
        super("Batik CleanerThread");
        setDaemon(true);
        start();
    }
    public void run() {
        while(true) {
            try {
                Reference ref;
                try {
                    ref = queue.remove();
                } catch (InterruptedException ie) {
                    continue;
                }
                if (ref instanceof ReferenceCleared) {
                    ReferenceCleared rc = (ReferenceCleared)ref;
                    rc.cleared();
                }
            } catch (ThreadDeath td) {
                throw td;
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
