package org.apache.tools.ant.util;
import java.io.*;
import junit.framework.TestCase;
public class ReaderInputStreamTest extends TestCase {
    public ReaderInputStreamTest(String s) {
        super(s);
    }
    public void testSimple() throws Exception {
        compareBytes("abc", "utf-8");
    }
    public void testSimple16() throws Exception {
        compareBytes("a", "utf-16");
    }
    public void testSimpleAbc16() throws Exception {
        byte[] bytes = new byte[40];
        int pos = 0;
        ReaderInputStream r = new ReaderInputStream(
            new StringReader("abc"), "utf-16");
        for (int i = 0; true; ++i) {
            int res = r.read();
            if (res == -1) {
                break;
            }
            bytes[pos++] = (byte) res;
        }
        bytes = "abc".getBytes("utf-16");
        String n = new String(bytes, 0, bytes.length, "utf-16");
        System.out.println(n);
    }
    public void testReadZero() throws Exception {
        ReaderInputStream r = new ReaderInputStream(
            new StringReader("abc"));
        byte[] bytes = new byte[30];
        r.read(bytes, 0, 0);
        int readin = r.read(bytes, 0, 10);
        assertEquals("abc".getBytes().length, readin);
    }
    public void testPreample() throws Exception {
        byte[] bytes = "".getBytes("utf-16");
        System.out.println("Preample len is " + bytes.length);
    }
    private void compareBytes(String s, String encoding) throws Exception {
        byte[] expected = s.getBytes(encoding);
        ReaderInputStream r = new ReaderInputStream(
            new StringReader(s), encoding);
        for (int i = 0; i < expected.length; ++i) {
            int expect = expected[i] & 0xFF;
            int read = r.read();
            if (expect != read) {
                fail("Mismatch in ReaderInputStream at index " + i
                     + " expecting " + expect + " got " + read + " for string "
                     + s + " with encoding " + encoding);
            }
        }
        if (r.read() != -1) {
            fail("Mismatch in ReaderInputStream - EOF not seen for string "
                 + s + " with encoding " + encoding);
        }
    }
}
