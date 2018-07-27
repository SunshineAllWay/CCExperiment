package org.apache.batik.util;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.batik.test.AbstractTest;
import org.apache.batik.test.TestReport;
public class RunnableQueueTest extends AbstractTest {
    public int nThreads;
    public int activeThreads;
    public Random rand;
    public RunnableQueue rq;
    public RunnableQueueTest(int nThreads) {
        this.nThreads = nThreads;
    }
    public RunnableQueueTest(Integer nThreads) {
        this((nThreads==null)?10:nThreads.intValue());
    }
        public String getName() {
            return "RunnableQueue Stress Test";
        }
    public TestReport runImpl() throws Exception {
        rq = RunnableQueue.createRunnableQueue();
        List l = new ArrayList(nThreads);
        rand = new Random(2345);
        l.add(new SwitchFlicker());
        l.add(new SwitchFlicker());
        for (int i=0; i<nThreads; i++) {
            Runnable rqRable = new RQRable(i, rand.nextInt(50)+1);
            l.add(new TPRable(rq, i, rand.nextInt(4)+1,
                              rand.nextInt(500)+1, 20, rqRable));
        }
        synchronized (this) {
            ThreadPounder tp = new ThreadPounder(l);
            tp.start();
            activeThreads = nThreads;
            while (activeThreads != 0) {
                wait();
            }
        }
        System.exit(0);
        return null;
    }
    public class SwitchFlicker implements Runnable {
        public void run() {
            boolean suspendp, waitp;
            int time;
            while (true) {
                try {
                    synchronized (rand) {
                        suspendp = rand.nextBoolean();
                        waitp = rand.nextBoolean();
                        time  = rand.nextInt(500);
                    }
                    if (suspendp) {
                        rq.suspendExecution(waitp);
                        System.out.println("Suspended - " + 
                                           (waitp?"Wait":"Later"));
                        Thread.sleep(time/10);
                    } else {
                        rq.resumeExecution();
                        System.out.println("Resumed");
                        Thread.sleep(time);
                    }
                } catch(InterruptedException ie) { }
            }
        }
    }
    public static final int INVOKE_LATER     = 1;
    public static final int INVOKE_AND_WAIT  = 2;
    public static final int PREEMPT_LATER    = 3;
    public static final int PREEMPT_AND_WAIT = 4;
    public class TPRable implements Runnable {
        RunnableQueue rq;
        int           idx;
        int           style;
        long          repeatDelay;
        int           count;
        Runnable      rqRable;
        TPRable(RunnableQueue rq, int idx, 
                int style,
                long    repeatDelay, int count,
                Runnable rqRable) {
            this.rq           = rq;
            this.idx          = idx;
            this.style        = style;
            this.repeatDelay  = repeatDelay;
            this.count        = count;
            this.rqRable      = rqRable;
        }
        public void run() {
            try {
                while (count-- != 0) {
                    switch (style) {
                    case INVOKE_LATER:
                        synchronized (rqRable) {
                            System.out.println("     InvL #" + idx);
                            rq.invokeLater(rqRable);
                            System.out.println("Done InvL #" + idx);
                            rqRable.wait();
                        }
                        break;
                    case INVOKE_AND_WAIT:
                        System.out.println("     InvW #" + idx);
                        rq.invokeAndWait(rqRable);
                        System.out.println("Done InvW #" + idx);
                        break;
                    case PREEMPT_LATER:
                        synchronized (rqRable) {
                            System.out.println("     PreL #" + idx);
                            rq.preemptLater(rqRable);
                            System.out.println("Done PreL #" + idx);
                            rqRable.wait();
                        }
                        break;
                    case PREEMPT_AND_WAIT:
                        System.out.println("     PreW #" + idx);
                        rq.preemptAndWait(rqRable);
                        System.out.println("Done PreW #" + idx);
                        break;
                    }
                    if (repeatDelay < 0) 
                        break;
                    Thread.sleep(repeatDelay);
                }
            } catch (InterruptedException ie) {
            }
            synchronized(RunnableQueueTest.this) {
                activeThreads--;
                RunnableQueueTest.this.notify();
            }
        }
    }
    public static class RQRable implements Runnable {
        int  idx;
        long dur;
        RQRable(int idx, long dur) {
            this.idx = idx;
            this.dur = dur;
        }
        public void run() {
            try {
                System.out.println("      B Rable #" + idx);
                Thread.sleep(dur);
                System.out.println("      E Rable #" + idx);
                synchronized (this) {
                    notify();
                }
            } catch (InterruptedException ie) { }
        }
    }
    public static void main(String []args) {
        RunnableQueueTest rqt = new RunnableQueueTest(20);
        try {
            rqt.runImpl();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
