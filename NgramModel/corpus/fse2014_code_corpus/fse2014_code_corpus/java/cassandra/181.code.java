package org.apache.cassandra.db.columniterator;
import java.io.IOError;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.IColumn;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.io.sstable.IndexHelper;
import org.apache.cassandra.io.sstable.SSTableReader;
import org.apache.cassandra.io.util.FileDataInput;
import org.apache.cassandra.io.util.FileMark;
import org.apache.cassandra.io.util.FileUtils;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.Filter;
public class SSTableNamesIterator extends SimpleAbstractColumnIterator implements IColumnIterator
{
    private static Logger logger = LoggerFactory.getLogger(SSTableNamesIterator.class);
    private ColumnFamily cf;
    private Iterator<IColumn> iter;
    public final SortedSet<ByteBuffer> columns;
    public final DecoratedKey key;
    public SSTableNamesIterator(SSTableReader sstable, DecoratedKey key, SortedSet<ByteBuffer> columns)
    {
        assert columns != null;
        this.columns = columns;
        this.key = key;
        FileDataInput file = sstable.getFileDataInput(key, DatabaseDescriptor.getIndexedReadBufferSizeInKB() * 1024);
        if (file == null)
            return;
        try
        {
            DecoratedKey keyInDisk = SSTableReader.decodeKey(sstable.partitioner,
                                                             sstable.descriptor,
                                                             ByteBufferUtil.readWithShortLength(file));
            assert keyInDisk.equals(key) : String.format("%s != %s in %s", keyInDisk, key, file.getPath());
            SSTableReader.readRowSize(file, sstable.descriptor);
            read(sstable, file);
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
        finally
        {
            FileUtils.closeQuietly(file);
        }
    }
    public SSTableNamesIterator(SSTableReader sstable, FileDataInput file, DecoratedKey key, SortedSet<ByteBuffer> columns)
    {
        assert columns != null;
        this.columns = columns;
        this.key = key;
        try
        {
            read(sstable, file);
        }
        catch (IOException ioe)
        {
            throw new IOError(ioe);
        }
    }
    private void read(SSTableReader sstable, FileDataInput file)
    throws IOException
    {
        Filter bf = IndexHelper.defreezeBloomFilter(file, sstable.descriptor.usesOldBloomFilter);
        List<IndexHelper.IndexInfo> indexList = IndexHelper.deserializeIndex(file);
        cf = ColumnFamily.serializer().deserializeFromSSTableNoColumns(ColumnFamily.create(sstable.metadata), file);
        List<ByteBuffer> filteredColumnNames = new ArrayList<ByteBuffer>(columns.size());
        for (ByteBuffer name : columns)
        {
            if (bf.isPresent(name))
            {
                filteredColumnNames.add(name);
            }
        }
        if (filteredColumnNames.isEmpty())
            return;
        if (indexList == null)
            readSimpleColumns(file, columns, filteredColumnNames);
        else
            readIndexedColumns(sstable.metadata, file, columns, filteredColumnNames, indexList);
        iter = cf.getSortedColumns().iterator();
    }
    private void readSimpleColumns(FileDataInput file, SortedSet<ByteBuffer> columnNames, List<ByteBuffer> filteredColumnNames) throws IOException
    {
        int columns = file.readInt();
        int n = 0;
        for (int i = 0; i < columns; i++)
        {
            IColumn column = cf.getColumnSerializer().deserialize(file);
            if (columnNames.contains(column.name()))
            {
                cf.addColumn(column);
                if (n++ > filteredColumnNames.size())
                    break;
            }
        }
    }
    private void readIndexedColumns(CFMetaData metadata, FileDataInput file, SortedSet<ByteBuffer> columnNames, List<ByteBuffer> filteredColumnNames, List<IndexHelper.IndexInfo> indexList)
    throws IOException
    {
        file.readInt(); 
        AbstractType comparator = metadata.comparator;
        SortedSet<IndexHelper.IndexInfo> ranges = new TreeSet<IndexHelper.IndexInfo>(IndexHelper.getComparator(comparator));
        for (ByteBuffer name : filteredColumnNames)
        {
            int index = IndexHelper.indexFor(name, indexList, comparator, false);
            if (index == indexList.size())
                continue;
            IndexHelper.IndexInfo indexInfo = indexList.get(index);
            if (comparator.compare(name, indexInfo.firstName) < 0)
                continue;
            ranges.add(indexInfo);
        }
        FileMark mark = file.mark();
        for (IndexHelper.IndexInfo indexInfo : ranges)
        {
            file.reset(mark);
            long curOffsert = file.skipBytes((int) indexInfo.offset);
            assert curOffsert == indexInfo.offset;
            while (file.bytesPastMark(mark) < indexInfo.offset + indexInfo.width)
            {
                IColumn column = cf.getColumnSerializer().deserialize(file);
                if (columnNames.contains(column.name()))
                {
                    cf.addColumn(column);
                }
            }
        }
    }
    public DecoratedKey getKey()
    {
        return key;
    }
    public ColumnFamily getColumnFamily()
    {
        return cf;
    }
    protected IColumn computeNext()
    {
        if (iter == null || !iter.hasNext())
            return endOfData();
        return iter.next();
    }
}
