package org.apache.cassandra.db.filter;
import java.util.Comparator;
import java.util.Iterator;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.db.*;
import org.apache.cassandra.db.columniterator.IColumnIterator;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.io.sstable.SSTableReader;
import org.apache.cassandra.io.util.FileDataInput;
public interface IFilter
{
    public abstract IColumnIterator getMemtableColumnIterator(ColumnFamily cf, DecoratedKey key, AbstractType comparator);
    public abstract IColumnIterator getSSTableColumnIterator(SSTableReader sstable, FileDataInput file, DecoratedKey key);
    public abstract IColumnIterator getSSTableColumnIterator(SSTableReader sstable, DecoratedKey key);
    public abstract void collectReducedColumns(IColumnContainer container, Iterator<IColumn> reducedColumns, int gcBefore);
    public abstract SuperColumn filterSuperColumn(SuperColumn superColumn, int gcBefore);
    public Comparator<IColumn> getColumnComparator(AbstractType comparator);
}
