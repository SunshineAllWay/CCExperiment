package org.apache.cassandra.db.marshal;
import java.nio.ByteBuffer;
import org.apache.cassandra.utils.ByteBufferUtil;
public class BytesType extends AbstractType
{
    public static final BytesType instance = new BytesType();
    BytesType() {} 
    public int compare(ByteBuffer o1, ByteBuffer o2)
    {
        if(null == o1){
            if(null == o2) return 0;
            else return -1;
        }
        return ByteBufferUtil.compareUnsigned(o1, o2);
    }
    public String getString(ByteBuffer bytes)
    {
        return ByteBufferUtil.bytesToHex(bytes);
    }
    public ByteBuffer fromString(String source)
    {
        return ByteBuffer.wrap(source.getBytes());
    }
    public void validate(ByteBuffer bytes) throws MarshalException
    {
    }
}
