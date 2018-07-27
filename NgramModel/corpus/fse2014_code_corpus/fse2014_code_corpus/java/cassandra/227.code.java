package org.apache.cassandra.dht;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.FBUtilities;
public class BytesToken extends Token<byte[]>
{
    public BytesToken(ByteBuffer token)
    {
        this(ByteBufferUtil.getArray(token));
    }
    public BytesToken(byte[] token)
    {
        super(token);
    }
    @Override
    public String toString()
    {
        return "Token(bytes[" + FBUtilities.bytesToHex(token) + "])";
    }
    @Override
    public int compareTo(Token<byte[]> o)
    {   
        return FBUtilities.compareUnsigned(token, o.token, 0, 0, token.length, o.token.length);
    }
    @Override
    public int hashCode()
    {
        final int prime = 31;
        return prime + Arrays.hashCode(token);
    }
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (!(obj instanceof BytesToken))
            return false;
        BytesToken other = (BytesToken) obj;
        return Arrays.equals(token, other.token);
    }
}
