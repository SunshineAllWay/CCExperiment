package org.apache.batik.util;
import java.util.Iterator;
import java.util.NoSuchElementException;
public class RunnableQueue implements Runnable {
    public static final class RunnableQueueState {
        private final String value;
        private RunnableQueueState(String value) {
            this.value = value; }
        public String getValue() { return value; }
        public String toString() {
            return "[RunnableQueueState: " + value + ']'; }
    }
    public static final RunnableQueueState RUNNING
        = new RunnableQueueState("Running");
    public static final RunnableQueueState SUSPENDING
        = new RunnableQueueState("Suspending");
    public static final RunnableQueueState SUSPENDED
        = new RunnableQueueState("Suspended");
    protected volatile RunnableQueueState state;
    protected final Object stateLock = new Object();
    protected boolean wasResumed;
    private final DoublyLinkedList list = new DoublyLinkedList();
    protected int preemptCount;
    protected RunHandler runHandler;
    protected volatile HaltingThread runnableQueueThread;
    private IdleRunnable idleRunnable;
    private long idleRunnableWaitTime;
    public static RunnableQueue createRunnableQueue() {
        RunnableQueue result = new RunnableQueue();
        synchronized (result) {
            HaltingThread ht = new HaltingThread
                (result, "RunnableQueue-" + threadCount++);
            ht.setDaemon(true);
            ht.start();
            while (result.getThread() == null) {
                try {
                    result.wait();
                } catch (InterruptedException ie) {
                }
            }
        }
        return result;
    }
    private static volatile int threadCount;
    public void run() {
        synchronized (this) {
            runnableQueueThread = (HaltingThread)Thread.currentThread();
            notify();
        }
        Link l;
        Runnable rable;
        try {
            while (!HaltingThread.hasBeenHalted()) {
                boolean callSuspended = false;
                boolean callResumed   = false;
                synchronized (stateLock) {
                    if (state != RUNNING) {
                        state = SUSPENDED;
                        callSuspended = true;
                    }
                }
                if (callSuspended)
                    executionSuspended();
                synchronized (stateLock) {
                    while (state != RUNNING) {
                        state = SUSPENDED;
                        stateLock.notifyAll();
                        try {
                            stateLock.wait();
                        } catch(InterruptedException ie) { }
                    }
                    if (wasResumed) {
                        wasResumed = false;
                        callResumed = true;
                    }
                }
                if (callResumed)
                    executionResumed();
                synchronized (list) {
                    if (state == SUSPENDING)
                        continue;
                    l = (Link)list.pop();
                    if (preemptCount != 0) preemptCount--;
                    if (l == null) {
                        if (idleRunnable != null &&
                                (idleRunnableWaitTime = idleRunnable.getWaitTime())
                                    < System.currentTimeMillis()) {
                            rable = idleRunnable;
                        } else {
                            try {
                                if (idleRunnable != null && idleRunnableWaitTime
                                        != Long.MAX_VALUE) {
                                    long t = idleRunnableWaitTime
                                        - System.currentTimeMillis();
                                    if (t > 0) {
                                        list.wait(t);
                                    }
                                } else {
                                    list.wait();
                                }
                            } catch (InterruptedException ie) {
                            }
                            continue; 
                        }
                    } else {
                        rable = l.runnable;
                    }
                }
                try {
                    runnableStart(rable);
                    rable.run();
                } catch (ThreadDeath td) {
                    throw td;
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                if (l != null) {
                    l.unlock();
                }
                try {
                    runnableInvoked(rable);
                } catch (ThreadDeath td) {
                    throw td;
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        } finally {
            do {
                synchronized (list) {
                    l = (Link)list.pop();
                }
                if (l == null) break;
                else           l.unlock();
            } while (true);
            synchronized (this) {
                runnableQueueThread = null;
            }
        }
    }
    public HaltingThread getThread() {
        return runnableQueueThread;
    }
    public void invokeLater(Runnable r) {
        if (runnableQueueThread == null) {
            throw new IllegalStateException
                ("RunnableQueue not started or has exited");
        }
        synchronized (list) {
            list.push(new Link(r));
            list.notify();
        }
    }
    public void invokeAndWait(Runnable r) throws InterruptedException {
        if (runnableQueueThread == null) {
            throw new IllegalStateException
                ("RunnableQueue not started or has exited");
        }
        if (runnableQueueThread == Thread.currentThread()) {
            throw new IllegalStateException
                ("Cannot be called from the RunnableQueue thread");
        }
        LockableLink l = new LockableLink(r);
        synchronized (list) {
            list.push(l);
            list.notify();
        }
        l.lock();           
    }
    public void preemptLater(Runnable r) {
        if (runnableQueueThread == null) {
            throw new IllegalStateException
                ("RunnableQueue not started or has exited");
        }
        synchronized (list) {
            list.add(preemptCount, new Link(r));
            preemptCount++;
            list.notify();
        }
    }
    public void preemptAndWait(Runnable r) throws InterruptedException {
        if (runnableQueueThread == null) {
            throw new IllegalStateException
                ("RunnableQueue not started or has exited");
        }
        if (runnableQueueThread == Thread.currentThread()) {
            throw new IllegalStateException
                ("Cannot be called from the RunnableQueue thread");
        }
        LockableLink l = new LockableLink(r);
        synchronized (list) {
            list.add(preemptCount, l);
            preemptCount++;
            list.notify();
        }
        l.lock();               
    }
    public RunnableQueueState getQueueState() {
        synchronized (stateLock) {
            return state;
        }
    }
    public void suspendExecution(boolean waitTillSuspended) {
        if (runnableQueueThread == null) {
            throw new IllegalStateException
                ("RunnableQueue not started or has exited");
        }
        synchronized (stateLock) {
            wasResumed = false;
            if (state == SUSPENDED) {
                stateLock.notifyAll();
                return;
            }
            if (state == RUNNING) {
                state = SUSPENDING;
                synchronized (list) {
                    list.notify();
                }
            }
            if (waitTillSuspended) {
                while (state == SUSPENDING) {
                    try {
                        stateLock.wait();
                    } catch(InterruptedException ie) { }
                }
            }
        }
    }
    public void resumeExecution() {
        if (runnableQueueThread == null) {
            throw new IllegalStateException
                ("RunnableQueue not started or has exited");
        }
        synchronized (stateLock) {
            wasResumed = true;
            if (state != RUNNING) {
                state = RUNNING;
                stateLock.notifyAll(); 
            }
        }
    }
    public Object getIteratorLock() {
        return list;
    }
    public Iterator iterator() {
        return new Iterator() {
                Link head = (Link)list.getHead();
                Link link;
                public boolean hasNext() {
                    if (head == null) {
                        return false;
                    }
                    if (link == null) {
                        return true;
                    }
                    return link != head;
                }
                public Object next() {
                    if (head == null || head == link) {
                        throw new NoSuchElementException();
                    }
                    if (link == null) {
                        link = (Link)head.getNext();
                        return head.runnable;
                    }
                    Object result = link.runnable;
                    link = (Link)link.getNext();
                    return result;
                }
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
    }
    public synchronized void setRunHandler(RunHandler rh) {
        runHandler = rh;
    }
    public synchronized RunHandler getRunHandler() {
        return runHandler;
    }
    public void setIdleRunnable(IdleRunnable r) {
        synchronized (list) {
            idleRunnable = r;
            idleRunnableWaitTime = 0;
            list.notify();
        }
    }
    protected synchronized void executionSuspended() {
        if (runHandler != null) {
            runHandler.executionSuspended(this);
        }
    }
    protected synchronized void executionResumed() {
        if (runHandler != null) {
            runHandler.executionResumed(this);
        }
    }
    protected synchronized void runnableStart(Runnable rable ) {
        if (runHandler != null) {
            runHandler.runnableStart(this, rable);
        }
    }
    protected synchronized void runnableInvoked(Runnable rable ) {
        if (runHandler != null) {
            runHandler.runnableInvoked(this, rable);
        }
    }
    public interface IdleRunnable extends Runnable {
        long getWaitTime();
    }
    public interface RunHandler {
        void runnableStart(RunnableQueue rq, Runnable r);
        void runnableInvoked(RunnableQueue rq, Runnable r);
        void executionSuspended(RunnableQueue rq);
        void executionResumed(RunnableQueue rq);
    }
    public static class RunHandlerAdapter implements RunHandler {
        public void runnableStart(RunnableQueue rq, Runnable r) { }
        public void runnableInvoked(RunnableQueue rq, Runnable r) { }
        public void executionSuspended(RunnableQueue rq) { }
        public void executionResumed(RunnableQueue rq) { }
    }
    protected static class Link extends DoublyLinkedList.Node {
        private final Runnable runnable;
        public Link(Runnable r) {
            runnable = r;
        }
        public void unlock() { return; }
    }
    protected static class LockableLink extends Link {
        private volatile boolean locked;
        public LockableLink(Runnable r) {
            super(r);
        }
        public boolean isLocked() {
            return locked;
        }
        public synchronized void lock() throws InterruptedException {
            locked = true;
            notify();
            wait();
        }
        public synchronized void unlock() {
            while (!locked) {
                try {
                    wait(); 
                } catch (InterruptedException ie) {
                }
            }
            locked = false;
            notify();
        }
    }
}
