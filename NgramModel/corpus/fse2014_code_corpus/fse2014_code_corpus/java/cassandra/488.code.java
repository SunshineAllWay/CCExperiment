package org.apache.cassandra.db.marshal;
import com.google.common.base.Charsets;
import org.apache.cassandra.Util;
import org.junit.Test;
import org.safehaus.uuid.UUIDGenerator;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Random;
public class TypeValidationTest
{
    @Test(expected = MarshalException.class)
    public void testInvalidAscii()
    {
        AsciiType.instance.validate(ByteBuffer.wrap(new byte[]{ (byte)0x80 }));
    }
    @Test(expected = MarshalException.class)
    public void testInvalidTimeUUID()
    {
        org.safehaus.uuid.UUID uuid = UUIDGenerator.getInstance().generateRandomBasedUUID();
        TimeUUIDType.instance.validate(ByteBuffer.wrap(uuid.toByteArray()));
    }
    @Test 
    public void testValidTimeUUID()
    {
        org.safehaus.uuid.UUID uuid = UUIDGenerator.getInstance().generateTimeBasedUUID();
        TimeUUIDType.instance.validate(ByteBuffer.wrap(uuid.toByteArray()));
    }
    @Test
    public void testLong()
    {
        LongType.instance.validate(Util.getBytes(5));
        LongType.instance.validate(Util.getBytes(5555555555555555555L));
    }
    @Test
    public void testValidUtf8() throws UnsupportedEncodingException
    {
        assert Character.MAX_CODE_POINT == 0x0010ffff;
        CharBuffer cb = CharBuffer.allocate(2837314);
        for (int i = 0; i < Character.MAX_CODE_POINT; i++)
        {
            if (i >= 55296 && i <= 57343)
                continue;
            char[] ch = Character.toChars(i);
            for (char c : ch)
                cb.append(c);
        }
        String s = new String(cb.array());
        byte[] arr = s.getBytes("UTF8");
        ByteBuffer buf = ByteBuffer.wrap(arr);
        UTF8Type.instance.validate(buf);
        UTF8Type.instance.validate(ByteBuffer.wrap(new byte[] {}));
        UTF8Type.instance.validate(ByteBuffer.wrap(new byte[] {0}));
        UTF8Type.instance.validate(ByteBuffer.wrap(new byte[] {99, (byte)0xc0, (byte)0x80, 112}));
        UTF8Type.instance.validate(ByteBuffer.wrap(new byte[] {(byte)0xc2, (byte)0x81}));
        UTF8Type.instance.validate(ByteBuffer.wrap(new byte[] {(byte)0xe0, (byte)0xa0, (byte)0x81}));
        UTF8Type.instance.validate(ByteBuffer.wrap(new byte[] {(byte)0xf0, (byte)0x90, (byte)0x81, (byte)0x81}));
    }
    @Test(expected = MarshalException.class)
    public void testFloatingc0()
    {
        UTF8Type.instance.validate(ByteBuffer.wrap(new byte[] {99, (byte)0xc0, 112}));
    }
    @Test(expected = MarshalException.class)
    public void testInvalid2nd()
    {
        UTF8Type.instance.validate(ByteBuffer.wrap(new byte[] {(byte)0xc2, (byte)0xff}));
    }
    @Test(expected = MarshalException.class)
    public void testInvalid3rd()
    {
        UTF8Type.instance.validate(ByteBuffer.wrap(new byte[] {(byte)0xe0, (byte)0xa0, (byte)0xff}));
    }
    @Test(expected = MarshalException.class)
    public void testInvalid4th()
    {
        UTF8Type.instance.validate(ByteBuffer.wrap(new byte[] {(byte)0xf0, (byte)0x90, (byte)0x81, (byte)0xff}));
    }
}
