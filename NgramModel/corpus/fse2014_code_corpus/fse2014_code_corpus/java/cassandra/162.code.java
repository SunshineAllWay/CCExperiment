package org.apache.cassandra.db;
import java.io.Closeable;
import java.io.IOError;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import org.apache.commons.collections.IteratorUtils;
import org.apache.cassandra.db.columniterator.IColumnIterator;
import org.apache.cassandra.db.filter.QueryFilter;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.io.sstable.SSTableReader;
import org.apache.cassandra.io.sstable.SSTableScanner;
import org.apache.cassandra.utils.ReducingIterator;
public class RowIteratorFactory
{
    private static final int RANGE_FILE_BUFFER_SIZE = 256 * 1024;
    private static final Comparator<IColumnIterator> COMPARE_BY_KEY = new Comparator<IColumnIterator>()
    {
        public int compare(IColumnIterator o1, IColumnIterator o2)
        {
            return DecoratedKey.comparator.compare(o1.getKey(), o2.getKey());
        }
    };
    public static RowIterator getIterator(final Collection<Memtable> memtables,
                                          final Collection<SSTableReader> sstables,
                                          final DecoratedKey startWith,
                                          final DecoratedKey stopAt,
                                          final QueryFilter filter,
                                          final AbstractType comparator,
                                          final ColumnFamilyStore cfs
    )
    {
        final List<Iterator<IColumnIterator>> iterators = new ArrayList<Iterator<IColumnIterator>>();
        Predicate<IColumnIterator> p = new Predicate<IColumnIterator>()
        {
            public boolean apply(IColumnIterator row)
            {
                return startWith.compareTo(row.getKey()) <= 0
                       && (stopAt.isEmpty() || row.getKey().compareTo(stopAt) <= 0);
            }
        };
        for (Memtable memtable : memtables)
        {
            iterators.add(Iterators.filter(Iterators.transform(memtable.getEntryIterator(startWith),
                                                               new ConvertToColumnIterator(filter, comparator)), p));
        }
        for (SSTableReader sstable : sstables)
        {
            final SSTableScanner scanner = sstable.getScanner(RANGE_FILE_BUFFER_SIZE, filter);
            scanner.seekTo(startWith);
            assert scanner instanceof Closeable; 
            iterators.add(scanner);
        }
        Iterator<IColumnIterator> collated = IteratorUtils.collatedIterator(COMPARE_BY_KEY, iterators);
        final Memtable firstMemtable = memtables.iterator().next();
        ReducingIterator<IColumnIterator, Row> reduced = new ReducingIterator<IColumnIterator, Row>(collated)
        {
            private final int gcBefore = (int) (System.currentTimeMillis() / 1000) - cfs.metadata.getGcGraceSeconds();
            private final List<IColumnIterator> colIters = new ArrayList<IColumnIterator>();
            private DecoratedKey key;
            public void reduce(IColumnIterator current)
            {
                this.colIters.add(current);
                this.key = current.getKey();
            }
            @Override
            protected boolean isEqual(IColumnIterator o1, IColumnIterator o2)
            {
                return COMPARE_BY_KEY.compare(o1, o2) == 0;
            }
            protected Row getReduced()
            {
                Comparator<IColumn> colComparator = filter.filter.getColumnComparator(comparator);
                Iterator<IColumn> colCollated = IteratorUtils.collatedIterator(colComparator, colIters);
                ColumnFamily returnCF;
                ColumnFamily cached = cfs.getRawCachedRow(key);
                if (cached != null)
                {
                    QueryFilter keyFilter = new QueryFilter(key, filter.path, filter.filter);
                    returnCF = cfs.filterColumnFamily(cached, keyFilter, gcBefore);
                }
                else if (colCollated.hasNext())
                {
                    returnCF = firstMemtable.getColumnFamily(key);
                    returnCF = returnCF == null ? ColumnFamily.create(firstMemtable.getTableName(), filter.getColumnFamilyName())
                                                : returnCF.cloneMeShallow();
                    long lastDeletedAt = Long.MIN_VALUE;
                    for (IColumnIterator columns : colIters)
                    {
                        columns.hasNext(); 
                        try
                        {
                            if (columns.getColumnFamily().isMarkedForDelete())
                                lastDeletedAt = Math.max(lastDeletedAt, columns.getColumnFamily().getMarkedForDeleteAt());
                        }
                        catch (IOException e)
                        {
                            throw new IOError(e);
                        }
                    }
                    returnCF.markedForDeleteAt.set(lastDeletedAt);
                    filter.collectCollatedColumns(returnCF, colCollated, gcBefore);
                }
                else
                {
                    returnCF = null;
                }
                Row rv = new Row(key, returnCF);
                colIters.clear();
                key = null;
                return rv;
            }
        };
        return new RowIterator(reduced, iterators);
    }
    private static Iterator<Map.Entry<DecoratedKey, ColumnFamily>> memtableEntryIterator(Memtable memtable, DecoratedKey startWith)
    {
        Table.flusherLock.readLock().lock();
        try
        {
            return memtable.getEntryIterator(startWith);
        }
        finally
        {
            Table.flusherLock.readLock().unlock();
        }
    }
    private static class ConvertToColumnIterator implements Function<Map.Entry<DecoratedKey, ColumnFamily>, IColumnIterator>
    {
        private QueryFilter filter;
        private AbstractType comparator;
        public ConvertToColumnIterator(QueryFilter filter, AbstractType comparator)
        {
            this.filter = filter;
            this.comparator = comparator;
        }
        public IColumnIterator apply(final Entry<DecoratedKey, ColumnFamily> entry)
        {
            return filter.getMemtableColumnIterator(entry.getValue(), entry.getKey(), comparator);
        }
    }
}
