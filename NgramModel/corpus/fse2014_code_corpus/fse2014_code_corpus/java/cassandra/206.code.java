package org.apache.cassandra.db.marshal;
import java.nio.ByteBuffer;
import java.util.UUID;
import org.apache.cassandra.utils.UUIDGen;
public class LexicalUUIDType extends AbstractType
{
    public static final LexicalUUIDType instance = new LexicalUUIDType();
    LexicalUUIDType() {} 
    public int compare(ByteBuffer o1, ByteBuffer o2)
    {
        if (o1.remaining() == 0)
        {
            return o2.remaining() == 0 ? 0 : -1;
        }
        if (o2.remaining() == 0)
        {
            return 1;
        }
        return UUIDGen.getUUID(o1).compareTo(UUIDGen.getUUID(o2));
    }
    public String getString(ByteBuffer bytes)
    {
        if (bytes.remaining() == 0)
        {
            return "";
        }
        if (bytes.remaining() != 16)
        {
            throw new MarshalException("UUIDs must be exactly 16 bytes");
        }
        return UUIDGen.getUUID(bytes).toString();
    }
    public ByteBuffer fromString(String source)
    {
        return ByteBuffer.wrap(UUIDGen.decompose(UUID.fromString(source)));
    }
    public void validate(ByteBuffer bytes) throws MarshalException
    {
        if (bytes.remaining() != 16 && bytes.remaining() != 0)
            throw new MarshalException(String.format("LexicalUUID should be 16 or 0 bytes (%d)", bytes.remaining()));
    }
}
