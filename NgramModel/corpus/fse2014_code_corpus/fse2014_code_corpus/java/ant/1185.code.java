package org.example.junit;
import junit.framework.TestCase;
public class MultilineAsserts extends TestCase {
    public void testFoo() { assertTrue("testFoo \nmessed up", false); }
    public void testBar() { assertTrue("testBar \ndidn't work", true); }
    public void testFee() { assertTrue("testFee \ncrashed", false); }
    public void testFie() { assertTrue("testFie \nbroke", true); }
}
