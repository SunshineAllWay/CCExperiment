package org.apache.cassandra.io.sstable;
import java.io.DataOutput;
import java.io.IOError;
import java.io.IOException;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.IColumn;
import org.apache.cassandra.db.columniterator.IColumnIterator;
import org.apache.cassandra.io.util.BufferedRandomAccessFile;
public class SSTableIdentityIterator implements Comparable<SSTableIdentityIterator>, IColumnIterator
{
    private final DecoratedKey key;
    private final long finishedAt;
    private final BufferedRandomAccessFile file;
    public final SSTableReader sstable;
    private final long dataStart;
    public final long dataSize;
    private final ColumnFamily columnFamily;
    public final int columnCount;
    private final long columnPosition;
    public SSTableIdentityIterator(SSTableReader sstable, BufferedRandomAccessFile file, DecoratedKey key, long dataStart, long dataSize)
    throws IOException
    {
        this.sstable = sstable;
        this.file = file;
        this.key = key;
        this.dataStart = dataStart;
        this.dataSize = dataSize;
        finishedAt = dataStart + dataSize;
        try
        {
            file.seek(this.dataStart);
            IndexHelper.skipBloomFilter(file);
            IndexHelper.skipIndex(file);
            columnFamily = sstable.createColumnFamily();
            ColumnFamily.serializer().deserializeFromSSTableNoColumns(columnFamily, file);
            columnCount = file.readInt();
            columnPosition = file.getFilePointer();
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
    }
    public DecoratedKey getKey()
    {
        return key;
    }
    public ColumnFamily getColumnFamily()
    {
        return columnFamily;
    }
    public boolean hasNext()
    {
        return file.getFilePointer() < finishedAt;
    }
    public IColumn next()
    {
        try
        {
            return sstable.getColumnSerializer().deserialize(file);
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
    }
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
    public void close() throws IOException
    {
    }
    public String getPath()
    {
        return file.getPath();
    }
    public void echoData(DataOutput out) throws IOException
    {
        file.seek(dataStart);
        while (file.getFilePointer() < finishedAt)
        {
            out.write(file.readByte());
        }
    }
    public ColumnFamily getColumnFamilyWithColumns() throws IOException
    {
        file.seek(columnPosition - 4); 
        ColumnFamily cf = columnFamily.cloneMeShallow();
        ColumnFamily.serializer().deserializeColumns(file, cf);
        return cf;
    }
    public int compareTo(SSTableIdentityIterator o)
    {
        return key.compareTo(o.key);
    }
    public void reset()
    {
        try
        {
            file.seek(columnPosition);
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
    }
}
