package org.apache.cassandra.db.filter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.collections.iterators.ReverseListIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.db.*;
import org.apache.cassandra.db.columniterator.IColumnIterator;
import org.apache.cassandra.db.columniterator.SSTableSliceIterator;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.io.sstable.SSTableReader;
import org.apache.cassandra.io.util.FileDataInput;
public class SliceQueryFilter implements IFilter
{
    private static Logger logger = LoggerFactory.getLogger(SliceQueryFilter.class);
    public final ByteBuffer start;
    public final ByteBuffer finish;
    public final boolean reversed;
    public final int count;
    public SliceQueryFilter(ByteBuffer start, ByteBuffer finish, boolean reversed, int count)
    {
        this.start = start;
        this.finish = finish;
        this.reversed = reversed;
        this.count = count;
    }
    public IColumnIterator getMemtableColumnIterator(ColumnFamily cf, DecoratedKey key, AbstractType comparator)
    {
        return Memtable.getSliceIterator(key, cf, this, comparator);
    }
    public IColumnIterator getSSTableColumnIterator(SSTableReader sstable, DecoratedKey key)
    {
        return new SSTableSliceIterator(sstable, key, start, finish, reversed);
    }
    public IColumnIterator getSSTableColumnIterator(SSTableReader sstable, FileDataInput file, DecoratedKey key)
    {
        return new SSTableSliceIterator(sstable, file, key, start, finish, reversed);
    }
    public SuperColumn filterSuperColumn(SuperColumn superColumn, int gcBefore)
    {
        SuperColumn scFiltered = superColumn.cloneMeShallow();
        Iterator<IColumn> subcolumns;
        if (reversed)
        {
            List<IColumn> columnsAsList = new ArrayList<IColumn>(superColumn.getSubColumns());
            subcolumns = new ReverseListIterator(columnsAsList);
        }
        else
        {
            subcolumns = superColumn.getSubColumns().iterator();
        }
        Comparator<ByteBuffer> comparator = reversed ? superColumn.getComparator().getReverseComparator() : superColumn.getComparator();
        while (subcolumns.hasNext())
        {
            IColumn column = subcolumns.next();
            if (comparator.compare(column.name(), start) >= 0)
            {
                subcolumns = IteratorUtils.chainedIterator(IteratorUtils.singletonIterator(column), subcolumns);
                break;
            }
        }
        collectReducedColumns(scFiltered, subcolumns, gcBefore);
        return scFiltered;
    }
    public Comparator<IColumn> getColumnComparator(AbstractType comparator)
    {
        return reversed ? new ReverseComparator(QueryFilter.getColumnComparator(comparator)) : QueryFilter.getColumnComparator(comparator);
    }
    public void collectReducedColumns(IColumnContainer container, Iterator<IColumn> reducedColumns, int gcBefore)
    {
        int liveColumns = 0;
        AbstractType comparator = container.getComparator();
        while (reducedColumns.hasNext())
        {
            if (liveColumns >= count)
                break;
            IColumn column = reducedColumns.next();
            if (logger.isDebugEnabled())
                logger.debug(String.format("collecting %s of %s: %s",
                                           liveColumns, count, column.getString(comparator)));
            if (finish.remaining() > 0
                && ((!reversed && comparator.compare(column.name(), finish) > 0))
                    || (reversed && comparator.compare(column.name(), finish) < 0))
                break;
            if (column.isLive() 
                && (!container.isMarkedForDelete()
                    || column.mostRecentLiveChangeAt() > container.getMarkedForDeleteAt()))
            {
                liveColumns++;
            }
            if (QueryFilter.isRelevant(column, container, gcBefore))
                container.addColumn(column);
        }
    }
}
