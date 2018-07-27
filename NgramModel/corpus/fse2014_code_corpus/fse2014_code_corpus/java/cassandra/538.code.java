package org.apache.cassandra.utils;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.util.Arrays;
import org.junit.Test;
public class FBUtilitiesTest 
{
	@Test
    public void testHexBytesConversion()
    {
        for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++)
        {
            byte[] b = new byte[]{ (byte)i };
            String s = FBUtilities.bytesToHex(b);
            byte[] c = FBUtilities.hexToBytes(s);
            assertArrayEquals(b, c);
        }
    }
    @Test
    public void testHexToBytesStringConversion()
    {
        String[] values = new String[]
        {
            "0",
            "10",
            "100",
            "101",
            "f",
            "ff"
        };
        byte[][] expected = new byte[][]
        {
            new byte[] { 0x00 },
            new byte[] { 0x10 },
            new byte[] { 0x01, 0x00 },
            new byte[] { 0x01, 0x01 },
            new byte[] { 0x0f },
            new byte[] { (byte)0x000000ff }
        };
        for (int i = 0; i < values.length; i++)
            assert Arrays.equals(FBUtilities.hexToBytes(values[i]), expected[i]);
    }
    @Test
    public void testIntBytesConversions()
    {
        int[] ints = new int[]
        {
            -20, -127, -128, 0, 1, 127, 128, 65534, 65535, -65534, -65535
        };
        for (int i : ints) {
            ByteBuffer ba = ByteBufferUtil.bytes(i);
            int actual = ByteBufferUtil.toInt(ba);
            assertEquals(i, actual);
        }
    }
    @Test
    public void testCopyIntoBytes()
    {
        int i = 300;
        long l = 1000;
        byte[] b = new byte[20];
        FBUtilities.copyIntoBytes(b, 0, i);
        FBUtilities.copyIntoBytes(b, 4, l);
        assertEquals(i, FBUtilities.byteArrayToInt(b, 0));
        assertEquals(l, FBUtilities.byteArrayToLong(b, 4));
    }
    @Test
    public void testLongBytesConversions()
    {
        long[] longs = new long[]
        {
            -20L, -127L, -128L, 0L, 1L, 127L, 128L, 65534L, 65535L, -65534L, -65535L,
            4294967294L, 4294967295L, -4294967294L, -4294967295L
        };
        for (long l : longs) {
            byte[] ba = FBUtilities.toByteArray(l);
            long actual = FBUtilities.byteArrayToLong(ba);
            assertEquals(l, actual);
        }
    }
    @Test
    public void testCompareByteSubArrays()
    {
        byte[] bytes = new byte[16];
        assert FBUtilities.compareByteSubArrays(
                null, 0, null, 0, 0) == 0;
        assert FBUtilities.compareByteSubArrays(
                null, 0, FBUtilities.toByteArray(524255231), 0, 4) == -1;
        assert FBUtilities.compareByteSubArrays(
                FBUtilities.toByteArray(524255231), 0, null, 0, 4) == 1;
        FBUtilities.copyIntoBytes(bytes, 3, 524255231);
        assert FBUtilities.compareByteSubArrays(
                bytes, 3, FBUtilities.toByteArray(524255231), 0, 4) == 0;
        assert FBUtilities.compareByteSubArrays(
                bytes, 3, FBUtilities.toByteArray(524255232), 0, 4) == -1;
        assert FBUtilities.compareByteSubArrays(
                bytes, 3, FBUtilities.toByteArray(524255230), 0, 4) == 1;
        try
        {
            assert FBUtilities.compareByteSubArrays(
                    bytes, 3, FBUtilities.toByteArray(524255231), 0, 24) == 0;
            fail("Should raise an AssertionError.");
        } catch (AssertionError ae)
        {
        }
        try
        {
            assert FBUtilities.compareByteSubArrays(
                    bytes, 3, FBUtilities.toByteArray(524255231), 0, 12) == 0;
            fail("Should raise an AssertionError.");
        } catch (AssertionError ae)
        {
        }
    }
    @Test(expected=CharacterCodingException.class)
    public void testDecode() throws IOException
    {
        ByteBuffer bytes = ByteBuffer.wrap(new byte[]{(byte)0xff, (byte)0xfe});
        FBUtilities.decodeToUTF8(bytes);
    } 
}
