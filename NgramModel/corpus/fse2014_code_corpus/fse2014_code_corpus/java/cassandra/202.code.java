package org.apache.cassandra.db.marshal;
import java.nio.ByteBuffer;
import com.google.common.base.Charsets;
import org.apache.cassandra.utils.ByteBufferUtil;
public class AsciiType extends BytesType
{
    public static final AsciiType instance = new AsciiType();
    AsciiType() {} 
    @Override
    public String getString(ByteBuffer bytes)
    {
        return ByteBufferUtil.string(bytes, Charsets.US_ASCII);
    }
    public ByteBuffer fromString(String source)
    {
        return ByteBuffer.wrap(source.getBytes(Charsets.US_ASCII));
    }
    public void validate(ByteBuffer bytes) throws MarshalException
    {
        for (int i = 0; i < bytes.remaining(); i++)
        {
            byte b = bytes.get(bytes.position() + i);
            if (b < 0 || b > 127)
                throw new MarshalException("Invalid byte for ascii: " + Byte.toString(b));
        }
    }
}
