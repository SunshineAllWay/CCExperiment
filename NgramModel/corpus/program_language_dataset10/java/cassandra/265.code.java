package org.apache.cassandra.io;
import java.io.DataOutput;
import java.io.IOException;
import java.security.MessageDigest;
import org.apache.cassandra.db.DecoratedKey;
public abstract class AbstractCompactedRow
{
    public final DecoratedKey key;
    public AbstractCompactedRow(DecoratedKey key)
    {
        this.key = key;
    }
    public abstract void write(DataOutput out) throws IOException;
    public abstract void update(MessageDigest digest);
    public abstract boolean isEmpty();
    public abstract int columnCount();
}
