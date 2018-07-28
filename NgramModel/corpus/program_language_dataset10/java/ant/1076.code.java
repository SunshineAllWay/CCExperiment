package org.apache.tools.ant.taskdefs.optional.junit;
import junit.framework.TestCase;
public class Sleeper extends TestCase {
    public Sleeper(String name) {
        super(name);
    }
    public void testSleep() {
        try {
            Thread.sleep(5 * 1000);
        } catch (InterruptedException e) {
        } 
    }
}
