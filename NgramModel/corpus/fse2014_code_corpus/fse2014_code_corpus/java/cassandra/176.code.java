package org.apache.cassandra.db.columniterator;
import java.io.IOException;
import java.util.Iterator;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.IColumn;
public interface IColumnIterator extends Iterator<IColumn>
{
    public abstract ColumnFamily getColumnFamily() throws IOException;
    public DecoratedKey getKey();
    public void close() throws IOException;
}
