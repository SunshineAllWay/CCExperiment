package org.apache.cassandra.io.sstable;
import java.io.Closeable;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.columniterator.IColumnIterator;
import org.apache.cassandra.db.filter.QueryFilter;
import org.apache.cassandra.io.util.BufferedRandomAccessFile;
import org.apache.cassandra.utils.ByteBufferUtil;
public class SSTableScanner implements Iterator<IColumnIterator>, Closeable
{
    private static Logger logger = LoggerFactory.getLogger(SSTableScanner.class);
    private final BufferedRandomAccessFile file;
    private final SSTableReader sstable;
    private IColumnIterator row;
    private boolean exhausted = false;
    private Iterator<IColumnIterator> iterator;
    private QueryFilter filter;
    SSTableScanner(SSTableReader sstable, int bufferSize, boolean skipCache)
    {
        try
        {
            this.file = new BufferedRandomAccessFile(new File(sstable.getFilename()), "r", bufferSize, skipCache);
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
        this.sstable = sstable;
    }
    SSTableScanner(SSTableReader sstable, QueryFilter filter, int bufferSize)
    {
        try
        {
            this.file = new BufferedRandomAccessFile(sstable.getFilename(), "r", bufferSize);
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
        this.sstable = sstable;
        this.filter = filter;
    }
    public void close() throws IOException
    {
        file.close();
    }
    public void seekTo(DecoratedKey seekKey)
    {
        try
        {
            long position = sstable.getPosition(seekKey, SSTableReader.Operator.GE);
            if (position < 0)
            {
                exhausted = true;
                return;
            }
            file.seek(position);
            row = null;
        }
        catch (IOException e)
        {
            throw new RuntimeException("corrupt sstable", e);
        }
    }
    public long getFileLength()
    {
        try
        {
            return file.length();
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
    }
    public long getFilePointer()
    {
        return file.getFilePointer();
    }
    public boolean hasNext()
    {
        if (iterator == null)
            iterator = exhausted ? Arrays.asList(new IColumnIterator[0]).iterator() : new KeyScanningIterator();
        return iterator.hasNext();
    }
    public IColumnIterator next()
    {
        if (iterator == null)
            iterator = exhausted ? Arrays.asList(new IColumnIterator[0]).iterator() : new KeyScanningIterator();
        return iterator.next();
    }
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
    private class KeyScanningIterator implements Iterator<IColumnIterator>
    {
        private long finishedAt;
        public boolean hasNext()
        {
            try
            {
                if (row == null)
                    return !file.isEOF();
                return finishedAt < file.length();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        public IColumnIterator next()
        {
            try
            {
                if (row != null)
                    file.seek(finishedAt);
                assert !file.isEOF();
                DecoratedKey key = SSTableReader.decodeKey(sstable.partitioner,
                                                           sstable.descriptor,
                                                           ByteBufferUtil.readWithShortLength(file));
                long dataSize = SSTableReader.readRowSize(file, sstable.descriptor);
                long dataStart = file.getFilePointer();
                finishedAt = dataStart + dataSize;
                if (filter == null)
                {
                    row = new SSTableIdentityIterator(sstable, file, key, dataStart, dataSize);
                    return row;
                }
                else
                {
                    return row = filter.getSSTableColumnIterator(sstable, file, key);
                }
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
