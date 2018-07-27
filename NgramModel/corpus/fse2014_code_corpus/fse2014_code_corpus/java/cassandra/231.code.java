package org.apache.cassandra.dht;
import java.nio.ByteBuffer;
import org.apache.cassandra.db.marshal.AbstractType;
public class LocalToken extends Token<ByteBuffer>
{
    private final AbstractType comparator;
    public LocalToken(AbstractType comparator, ByteBuffer token)
    {
        super(token);
        this.comparator = comparator;
    }
    @Override
    public String toString()
    {
        return comparator.getString(token);
    }
    @Override
    public int compareTo(Token<ByteBuffer> o)
    {
        return comparator.compare(token, o.token);
    }
    @Override
    public int hashCode()
    {
        final int prime = 31;
        return prime + token.hashCode();
    }
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (!(obj instanceof LocalToken))
            return false;
        LocalToken other = (LocalToken) obj;
        return token.equals(other.token);
    }
}
