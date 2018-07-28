package org.apache.cassandra.io.sstable;
import java.io.Closeable;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.Iterator;
import com.google.common.collect.AbstractIterator;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.io.util.BufferedRandomAccessFile;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.ByteBufferUtil;
public class KeyIterator extends AbstractIterator<DecoratedKey> implements Iterator<DecoratedKey>, Closeable
{
    private final BufferedRandomAccessFile in;
    private final Descriptor desc;
    public KeyIterator(Descriptor desc)
    {
        this.desc = desc;
        try
        {
            in = new BufferedRandomAccessFile(new File(desc.filenameFor(SSTable.COMPONENT_INDEX)),
                                              "r",
                                              BufferedRandomAccessFile.DEFAULT_BUFFER_SIZE,
                                              true);
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
    }
    protected DecoratedKey computeNext()
    {
        try
        {
            if (in.isEOF())
                return endOfData();
            DecoratedKey key = SSTableReader.decodeKey(StorageService.getPartitioner(), desc, ByteBufferUtil.readWithShortLength(in));
            in.readLong(); 
            return key;
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
    }
    public void close() throws IOException
    {
        in.close();
    }
    public long getBytesRead()
    {
        return in.getFilePointer();
    }
    public long getTotalBytes()
    {
        try
        {
            return in.length();
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
    }
}
