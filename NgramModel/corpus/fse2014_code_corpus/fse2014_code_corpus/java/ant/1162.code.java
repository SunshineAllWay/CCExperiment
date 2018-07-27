package org.apache.tools.ant.util.facade;
import junit.framework.TestCase;
public class FacadeTaskHelperTest extends TestCase {
    public FacadeTaskHelperTest(String name) {
        super(name);
    }
    public void testPrecedenceRules() {
        FacadeTaskHelper fth = new FacadeTaskHelper("foo");
        assertEquals("foo", fth.getImplementation());
        fth.setMagicValue("bar");
        assertEquals("bar", fth.getImplementation());
        fth = new FacadeTaskHelper("foo", "bar");
        assertEquals("bar", fth.getImplementation());
        fth = new FacadeTaskHelper("foo", null);
        assertEquals("foo", fth.getImplementation());
        fth = new FacadeTaskHelper("foo");
        fth.setMagicValue("bar");
        fth.setImplementation("baz");
        assertEquals("baz", fth.getImplementation());
    }
    public void testHasBeenSet() {
        FacadeTaskHelper fth = new FacadeTaskHelper("foo");
        assertTrue("nothing set", !fth.hasBeenSet());
        fth.setMagicValue(null);
        assertTrue("magic has not been set", !fth.hasBeenSet());
        fth.setMagicValue("foo");
        assertTrue("magic has been set", fth.hasBeenSet());
        fth.setMagicValue(null);
        assertTrue(!fth.hasBeenSet());
        fth.setImplementation("baz");
        assertTrue("set explicitly", fth.hasBeenSet());
    }
}
