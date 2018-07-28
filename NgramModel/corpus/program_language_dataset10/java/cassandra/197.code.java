package org.apache.cassandra.db.filter;
import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.db.*;
import org.apache.cassandra.db.columniterator.IColumnIterator;
import org.apache.cassandra.db.columniterator.IdentityQueryFilter;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.io.sstable.SSTableReader;
import org.apache.cassandra.io.util.FileDataInput;
import org.apache.cassandra.thrift.SlicePredicate;
import org.apache.cassandra.thrift.SliceRange;
import org.apache.cassandra.utils.ReducingIterator;
public class QueryFilter
{
    private static Logger logger = LoggerFactory.getLogger(QueryFilter.class);
    public final DecoratedKey key;
    public final QueryPath path;
    public final IFilter filter;
    private final IFilter superFilter;
    public QueryFilter(DecoratedKey key, QueryPath path, IFilter filter)
    {
        this.key = key;
        this.path = path;
        this.filter = filter;
        superFilter = path.superColumnName == null ? null : new NamesQueryFilter(path.superColumnName);
    }
    public IColumnIterator getMemtableColumnIterator(Memtable memtable, AbstractType comparator)
    {
        ColumnFamily cf = memtable.getColumnFamily(key);
        if (cf == null)
            return null;
        return getMemtableColumnIterator(cf, key, comparator);
    }
    public IColumnIterator getMemtableColumnIterator(ColumnFamily cf, DecoratedKey key, AbstractType comparator)
    {
        assert cf != null;
        if (path.superColumnName == null)
            return filter.getMemtableColumnIterator(cf, key, comparator);
        return superFilter.getMemtableColumnIterator(cf, key, comparator);
    }
    public IColumnIterator getSSTableColumnIterator(SSTableReader sstable)
    {
        if (path.superColumnName == null)
            return filter.getSSTableColumnIterator(sstable, key);
        return superFilter.getSSTableColumnIterator(sstable, key);
    }
    public IColumnIterator getSSTableColumnIterator(SSTableReader sstable, FileDataInput file, DecoratedKey key)
    {
        if (path.superColumnName == null)
            return filter.getSSTableColumnIterator(sstable, file, key);
        return superFilter.getSSTableColumnIterator(sstable, file, key);
    }
    static Comparator<IColumn> getColumnComparator(final Comparator<ByteBuffer> comparator)
    {
        return new Comparator<IColumn>()
        {
            public int compare(IColumn c1, IColumn c2)
            {
                return comparator.compare(c1.name(), c2.name());
            }
        };
    }
    public void collectCollatedColumns(final ColumnFamily returnCF, Iterator<IColumn> collatedColumns, final int gcBefore)
    {
        ReducingIterator<IColumn, IColumn> reduced = new ReducingIterator<IColumn, IColumn>(collatedColumns)
        {
            ColumnFamily curCF = returnCF.cloneMeShallow();
            protected boolean isEqual(IColumn o1, IColumn o2)
            {
                return o1.name().equals(o2.name());
            }
            public void reduce(IColumn current)
            {
                curCF.addColumn(current);
            }
            protected IColumn getReduced()
            {
                IColumn c = curCF.getSortedColumns().iterator().next();
                if (superFilter != null)
                {
                    long deletedAt = c.getMarkedForDeleteAt();
                    if (returnCF.getMarkedForDeleteAt() > deletedAt)
                        ((SuperColumn)c).markForDeleteAt(c.getLocalDeletionTime(), returnCF.getMarkedForDeleteAt());
                    c = filter.filterSuperColumn((SuperColumn)c, gcBefore);
                    ((SuperColumn)c).markForDeleteAt(c.getLocalDeletionTime(), deletedAt); 
                }
                curCF.clear();           
                return c;
            }
        };
        (superFilter == null ? filter : superFilter).collectReducedColumns(returnCF, reduced, gcBefore);
    }
    public String getColumnFamilyName()
    {
        return path.columnFamilyName;
    }
    public static boolean isRelevant(IColumn column, IColumnContainer container, int gcBefore)
    {
        long maxChange = column.mostRecentLiveChangeAt();
        return (!column.isMarkedForDelete() || column.getLocalDeletionTime() > gcBefore || maxChange > column.getMarkedForDeleteAt()) 
               && (!container.isMarkedForDelete() || maxChange > container.getMarkedForDeleteAt()); 
    }
    public static QueryFilter getSliceFilter(DecoratedKey key, QueryPath path, ByteBuffer start, ByteBuffer finish, boolean reversed, int limit)
    {
        return new QueryFilter(key, path, new SliceQueryFilter(start, finish, reversed, limit));
    }
    public static QueryFilter getIdentityFilter(DecoratedKey key, QueryPath path)
    {
        return new QueryFilter(key, path, new IdentityQueryFilter());
    }
    public static QueryFilter getNamesFilter(DecoratedKey key, QueryPath path, SortedSet<ByteBuffer> columns)
    {
        return new QueryFilter(key, path, new NamesQueryFilter(columns));
    }
    public static IFilter getFilter(SlicePredicate predicate, AbstractType comparator)
    {
        if (predicate.column_names != null)
        {
            final SortedSet<ByteBuffer> columnNameSet = new TreeSet<ByteBuffer>(comparator);
            columnNameSet.addAll(predicate.column_names);
            return new NamesQueryFilter(columnNameSet);
        }
        SliceRange range = predicate.slice_range;
        return new SliceQueryFilter(range.start, range.finish, range.reversed, range.count);
    }
    public static QueryFilter getNamesFilter(DecoratedKey key, QueryPath path, ByteBuffer column)
    {
        return new QueryFilter(key, path, new NamesQueryFilter(column));
    }
}
