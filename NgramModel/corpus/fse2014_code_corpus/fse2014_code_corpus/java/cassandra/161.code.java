package org.apache.cassandra.db;
import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.apache.cassandra.db.columniterator.IColumnIterator;
import org.apache.cassandra.utils.ReducingIterator;
public class RowIterator implements Closeable, Iterator<Row>
{
    private ReducingIterator<IColumnIterator, Row> reduced;
    private List<Iterator<IColumnIterator>> iterators;
    public RowIterator(ReducingIterator<IColumnIterator, Row> reduced, List<Iterator<IColumnIterator>> iterators)
    {
        this.reduced = reduced;
        this.iterators = iterators;
    }
    public boolean hasNext()
    {
        return reduced.hasNext();
    }
    public Row next()
    {
        return reduced.next();
    }
    public void remove()
    {
        reduced.remove();
    }
    public void close() throws IOException
    {
        for (Iterator iter : iterators)
        {
            if (iter instanceof Closeable)
            {
                ((Closeable)iter).close();
            }
        }
	}
}
