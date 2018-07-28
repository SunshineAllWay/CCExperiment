package org.apache.tools.zip;
import junit.framework.TestCase;
public class ZipLongTest extends TestCase {
    public ZipLongTest(String name) {
        super(name);
    }
    public void testToBytes() {
        ZipLong zl = new ZipLong(0x12345678);
        byte[] result = zl.getBytes();
        assertEquals("length getBytes", 4, result.length);
        assertEquals("first byte getBytes", 0x78, result[0]);
        assertEquals("second byte getBytes", 0x56, result[1]);
        assertEquals("third byte getBytes", 0x34, result[2]);
        assertEquals("fourth byte getBytes", 0x12, result[3]);
    }
    public void testFromBytes() {
        byte[] val = new byte[] {0x78, 0x56, 0x34, 0x12};
        ZipLong zl = new ZipLong(val);
        assertEquals("value from bytes", 0x12345678, zl.getValue());
    }
    public void testEquals() {
        ZipLong zl = new ZipLong(0x12345678);
        ZipLong zl2 = new ZipLong(0x12345678);
        ZipLong zl3 = new ZipLong(0x87654321);
        assertTrue("reflexive", zl.equals(zl));
        assertTrue("works", zl.equals(zl2));
        assertTrue("works, part two", !zl.equals(zl3));
        assertTrue("symmetric", zl2.equals(zl));
        assertTrue("null handling", !zl.equals(null));
        assertTrue("non ZipLong handling", !zl.equals(new Integer(0x1234)));
    }
    public void testSign() {
        ZipLong zl = new ZipLong(new byte[] {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF});
        assertEquals(0x00000000FFFFFFFFl, zl.getValue());
    }
    public void testClone() {
        ZipLong s1 = new ZipLong(42);
        ZipLong s2 = (ZipLong) s1.clone();
        assertNotSame(s1, s2);
        assertEquals(s1, s2);
        assertEquals(s1.getValue(), s2.getValue());
    }
}
