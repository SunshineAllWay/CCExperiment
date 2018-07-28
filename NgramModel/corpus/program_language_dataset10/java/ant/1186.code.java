package org.example.junit;
import junit.framework.TestCase;
public class Output extends TestCase {
    public Output(String s) {
        super(s);
    }
    public void testOutput() {
        System.out.println("foo");
    }
}
