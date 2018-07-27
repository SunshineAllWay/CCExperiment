package org.apache.cassandra.db.columniterator;
import java.io.IOException;
import com.google.common.collect.AbstractIterator;
import org.apache.cassandra.db.IColumn;
public abstract class SimpleAbstractColumnIterator extends AbstractIterator<IColumn> implements IColumnIterator
{
    public void close() throws IOException {}
}
