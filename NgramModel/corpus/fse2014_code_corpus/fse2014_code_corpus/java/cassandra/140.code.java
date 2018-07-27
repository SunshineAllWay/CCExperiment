package org.apache.cassandra.db;
import java.nio.ByteBuffer;
import java.util.Comparator;
import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.ByteBufferUtil;
public class DecoratedKey<T extends Token> implements Comparable<DecoratedKey>
{
    private static IPartitioner partitioner = StorageService.getPartitioner();
    public static final Comparator<DecoratedKey> comparator = new Comparator<DecoratedKey>()
    {
        public int compare(DecoratedKey o1, DecoratedKey o2)
        {
            return o1.compareTo(o2);
        }
    };
    public final T token;
    public final ByteBuffer key;
    public DecoratedKey(T token, ByteBuffer key)
    {
        super();
        assert token != null;
        this.token = token;
        this.key = key;
    }
    @Override
    public int hashCode()
    {
        return token.hashCode();
    }
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DecoratedKey other = (DecoratedKey) obj;
        return token.equals(other.token);
    }
    public int compareTo(DecoratedKey other)
    {
        return token.compareTo(other.token);
    }
    public boolean isEmpty()
    {
        return token.equals(partitioner.getMinimumToken());
    }
    @Override
    public String toString()
    {
        String keystring = key == null ? "null" : ByteBufferUtil.bytesToHex(key);
        return "DecoratedKey(" + token + ", " + keystring + ")";
    }
}
