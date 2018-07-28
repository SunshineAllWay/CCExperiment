package org.apache.cassandra.db.filter;
import java.io.IOException;
import org.apache.cassandra.db.columniterator.IColumnIterator;
public abstract class AbstractColumnIterator implements IColumnIterator
{
    public void close() throws IOException
    {}
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
