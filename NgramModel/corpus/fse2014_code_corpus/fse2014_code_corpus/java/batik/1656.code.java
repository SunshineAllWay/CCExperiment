package org.apache.batik.util;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Collections;
import java.util.Random;
public class ThreadPounder {
    List runnables;
    Object [] threads;
    Object lock = new Object();
    public ThreadPounder(List runnables)  
        throws InterruptedException {
        this(runnables, new Random(1234));
    }
    public ThreadPounder(List runnables, Random rand) 
        throws InterruptedException {
        this.runnables = new ArrayList(runnables);
        Collections.shuffle(this.runnables, rand);
        threads = new Object[this.runnables.size()];
        int i=0;
        Iterator iter= this.runnables.iterator();
        synchronized (lock) {
            while (iter.hasNext()) {
                Thread t = new SyncThread((Runnable)iter.next());
                t.start();
                lock.wait();
                threads[i] = t;
                i++;
            }
        }
    }
    public void start() {
        synchronized(this) {
            this.notifyAll();
        }
    }
    class SyncThread extends Thread {
        Runnable toRun;
        public long runTime;
        public SyncThread(Runnable toRun) {
            this.toRun = toRun;
        }
        public void run() {
            try {
                synchronized (ThreadPounder.this) {
                    synchronized (lock) {
                        lock.notify();
                    }
                    ThreadPounder.this.wait();
                }
                toRun.run();
            } catch (InterruptedException ie) {
            }
        }
    }
    public static void main(String [] str) { 
        List l = new ArrayList(20);
        for (int i=0; i<20; i++) {
            final int x = i;
            l.add(new Runnable() {
                    public void run() {
                        System.out.println("Thread " + x);
                    }
                });
        }
        try { 
            ThreadPounder tp = new ThreadPounder(l);
            System.out.println("Starting:" );
            tp.start();
            System.out.println("All Started:" );
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }
}
