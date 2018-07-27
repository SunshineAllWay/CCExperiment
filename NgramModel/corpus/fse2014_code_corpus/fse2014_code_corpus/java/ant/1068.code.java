package org.apache.tools.ant.taskdefs.optional.junit;
import junit.framework.TestCase;
public class JUnitClassLoaderTest extends TestCase {
    public JUnitClassLoaderTest(String s) {
        super(s);
    }
    public void testContextClassLoader(){
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        ClassLoader caller = getClass().getClassLoader();
        assertSame(context, caller);
    }
}
