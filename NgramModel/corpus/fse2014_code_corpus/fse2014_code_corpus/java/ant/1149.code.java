package org.apache.tools.ant.util;
import java.io.File;
import java.io.IOException;
import junit.framework.TestCase;
public class LazyFileOutputStreamTest extends TestCase {
    private LazyFileOutputStream los;
    private final static File f = new File("test.txt");
    public LazyFileOutputStreamTest(String s) {
        super(s);
    }
    public void setUp() {
        los = new LazyFileOutputStream(f);
    }
    public void tearDown() throws IOException {
        try {
            los.close();
        } finally {
            f.delete();
        }
    }
    public void testNoFileWithoutWrite() throws IOException {
        los.close();
        assertTrue(f + " has not been written.", !f.exists());
    }
    public void testOpen() throws IOException {
        los.open();
        los.close();
        assertTrue(f + " has been written.", f.exists());
    }
    public void testSingleByte() throws IOException {
        los.write(0);
        los.close();
        assertTrue(f + " has been written.", f.exists());
    }
    public void testByteArray() throws IOException {
        los.write(new byte[] {0});
        los.close();
        assertTrue(f + " has been written.", f.exists());
    }
}
