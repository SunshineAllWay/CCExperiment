package org.apache.tools.ant.util.facade;
import junit.framework.TestCase;
public class ImplementationSpecificArgumentTest extends TestCase {
    public ImplementationSpecificArgumentTest(String name) {
        super(name);
    }
    public void testDependsOnImplementation() {
        ImplementationSpecificArgument ia =
            new ImplementationSpecificArgument();
        ia.setLine("A B");
        String[] parts = ia.getParts();
        assertNotNull(parts);
        assertEquals(2, parts.length);
        assertEquals("A", parts[0]);
        assertEquals("B", parts[1]);
        parts = ia.getParts(null);
        assertNotNull(parts);
        assertEquals(2, parts.length);
        assertEquals("A", parts[0]);
        assertEquals("B", parts[1]);
        ia.setImplementation("foo");
        parts = ia.getParts(null);
        assertNotNull(parts);
        assertEquals(0, parts.length);
        parts = ia.getParts("foo");
        assertNotNull(parts);
        assertEquals(2, parts.length);
        assertEquals("A", parts[0]);
        assertEquals("B", parts[1]);
    }
}
