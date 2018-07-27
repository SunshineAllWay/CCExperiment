package org.example.junit;
import junit.framework.TestCase;
public class ThreadedOutput extends TestCase {
    public ThreadedOutput(String s) {
        super(s);
    }
    public void testOutput() throws InterruptedException {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    System.out.println("foo");
                }
            });
        t.start();
        t.join();
    }
}
