package org.apache.tools.zip;
import junit.framework.TestCase;
public class ZipShortTest extends TestCase {
    public ZipShortTest(String name) {
        super(name);
    }
    public void testToBytes() {
        ZipShort zs = new ZipShort(0x1234);
        byte[] result = zs.getBytes();
        assertEquals("length getBytes", 2, result.length);
        assertEquals("first byte getBytes", 0x34, result[0]);
        assertEquals("second byte getBytes", 0x12, result[1]);
    }
    public void testFromBytes() {
        byte[] val = new byte[] {0x34, 0x12};
        ZipShort zs = new ZipShort(val);
        assertEquals("value from bytes", 0x1234, zs.getValue());
    }
    public void testEquals() {
        ZipShort zs = new ZipShort(0x1234);
        ZipShort zs2 = new ZipShort(0x1234);
        ZipShort zs3 = new ZipShort(0x5678);
        assertTrue("reflexive", zs.equals(zs));
        assertTrue("works", zs.equals(zs2));
        assertTrue("works, part two", !zs.equals(zs3));
        assertTrue("symmetric", zs2.equals(zs));
        assertTrue("null handling", !zs.equals(null));
        assertTrue("non ZipShort handling", !zs.equals(new Integer(0x1234)));
    }
    public void testSign() {
        ZipShort zs = new ZipShort(new byte[] {(byte)0xFF, (byte)0xFF});
        assertEquals(0x0000FFFF, zs.getValue());
    }
    public void testClone() {
        ZipShort s1 = new ZipShort(42);
        ZipShort s2 = (ZipShort) s1.clone();
        assertNotSame(s1, s2);
        assertEquals(s1, s2);
        assertEquals(s1.getValue(), s2.getValue());
    }
}
