package org.apache.tools.tar;
import junit.framework.TestCase;
public class TarEntryTest extends TestCase {
    public TarEntryTest(String name) {
        super(name);
    }
    public void testFileConstructor() {
        new TarEntry(new java.io.File("/foo"));
    }
}
