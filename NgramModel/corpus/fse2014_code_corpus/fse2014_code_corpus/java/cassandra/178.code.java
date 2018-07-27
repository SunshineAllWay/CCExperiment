package org.apache.cassandra.db.columniterator;
import java.io.IOError;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import com.google.common.collect.AbstractIterator;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.IColumn;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.io.sstable.IndexHelper;
import org.apache.cassandra.io.util.FileDataInput;
import org.apache.cassandra.io.util.FileMark;
import org.apache.cassandra.io.sstable.SSTableReader;
class IndexedSliceReader extends AbstractIterator<IColumn> implements IColumnIterator
{
    private final ColumnFamily emptyColumnFamily;
    private final List<IndexHelper.IndexInfo> indexes;
    private final FileDataInput file;
    private final ByteBuffer startColumn;
    private final ByteBuffer finishColumn;
    private final boolean reversed;
    private BlockFetcher fetcher;
    private Deque<IColumn> blockColumns = new ArrayDeque<IColumn>();
    private AbstractType comparator;
    public IndexedSliceReader(SSTableReader sstable, FileDataInput input, ByteBuffer startColumn, ByteBuffer finishColumn, boolean reversed)
    {
        this.file = input;
        this.startColumn = startColumn;
        this.finishColumn = finishColumn;
        this.reversed = reversed;
        comparator = sstable.metadata.comparator;
        try
        {
            IndexHelper.skipBloomFilter(file);
            indexes = IndexHelper.deserializeIndex(file);
            emptyColumnFamily = ColumnFamily.serializer().deserializeFromSSTableNoColumns(ColumnFamily.create(sstable.metadata), file);
            fetcher = indexes == null ? new SimpleBlockFetcher() : new IndexedBlockFetcher();
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
    }
    public ColumnFamily getColumnFamily()
    {
        return emptyColumnFamily;
    }
    public DecoratedKey getKey()
    {
        throw new UnsupportedOperationException();
    }
    private boolean isColumnNeeded(IColumn column)
    {
        if (startColumn.remaining() == 0 && finishColumn.remaining() == 0)
            return true;
        else if (startColumn.remaining() == 0 && !reversed)
            return comparator.compare(column.name(), finishColumn) <= 0;
        else if (startColumn.remaining() == 0 && reversed)
            return comparator.compare(column.name(), finishColumn) >= 0;
        else if (finishColumn.remaining() == 0 && !reversed)
            return comparator.compare(column.name(), startColumn) >= 0;
        else if (finishColumn.remaining() == 0 && reversed)
            return comparator.compare(column.name(), startColumn) <= 0;
        else if (!reversed)
            return comparator.compare(column.name(), startColumn) >= 0 && comparator.compare(column.name(), finishColumn) <= 0;
        else 
            return comparator.compare(column.name(), startColumn) <= 0 && comparator.compare(column.name(), finishColumn) >= 0;
    }
    protected IColumn computeNext()
    {
        while (true)
        {
            IColumn column = blockColumns.poll();
            if (column != null && isColumnNeeded(column))
                return column;
            try
            {
                if (column == null && !fetcher.getNextBlock())
                    return endOfData();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
    public void close()
    {
    }
    interface BlockFetcher
    {
        public boolean getNextBlock() throws IOException;
    }
    private class IndexedBlockFetcher implements BlockFetcher
    {
        private final FileMark mark;
        private int curRangeIndex;
        IndexedBlockFetcher() throws IOException
        {
            file.readInt(); 
            this.mark = file.mark();
            curRangeIndex = IndexHelper.indexFor(startColumn, indexes, comparator, reversed);
            if (reversed && curRangeIndex == indexes.size())
                curRangeIndex--;
        }
        public boolean getNextBlock() throws IOException
        {
            if (curRangeIndex < 0 || curRangeIndex >= indexes.size())
                return false;
            IndexHelper.IndexInfo curColPosition = indexes.get(curRangeIndex);
            if (reversed)
            {
                if ((finishColumn.remaining() > 0 && comparator.compare(finishColumn, curColPosition.lastName) > 0) ||
                    (startColumn.remaining() > 0 && comparator.compare(startColumn, curColPosition.firstName) < 0))
                    return false;
            }
            else
            {
                if ((startColumn.remaining() > 0 && comparator.compare(startColumn, curColPosition.lastName) > 0) ||
                    (finishColumn.remaining() > 0 && comparator.compare(finishColumn, curColPosition.firstName) < 0))
                    return false;
            }
            boolean outOfBounds = false;
            file.reset(mark);
            long curOffset = file.skipBytes((int) curColPosition.offset);
            assert curOffset == curColPosition.offset;
            while (file.bytesPastMark(mark) < curColPosition.offset + curColPosition.width && !outOfBounds)
            {
                IColumn column = emptyColumnFamily.getColumnSerializer().deserialize(file);
                if (reversed)
                    blockColumns.addFirst(column);
                else
                    blockColumns.addLast(column);
                if (!reversed && finishColumn.remaining() > 0)
                    outOfBounds = comparator.compare(column.name(), finishColumn) >= 0;
                else if (reversed && startColumn.remaining() > 0)
                    outOfBounds = comparator.compare(column.name(), startColumn) >= 0;
            }
            if (reversed)
                curRangeIndex--;
            else
                curRangeIndex++;
            return true;
        }
    }
    private class SimpleBlockFetcher implements BlockFetcher
    {
        private SimpleBlockFetcher() throws IOException
        {
            int columns = file.readInt();
            for (int i = 0; i < columns; i++)
            {
                IColumn column = emptyColumnFamily.getColumnSerializer().deserialize(file);
                if (reversed)
                    blockColumns.addFirst(column);
                else
                    blockColumns.addLast(column);
                boolean outOfBounds = false;
                if (!reversed && finishColumn.remaining() > 0)
                    outOfBounds = comparator.compare(column.name(), finishColumn) >= 0;
                else if (reversed && startColumn.remaining() > 0)
                    outOfBounds = comparator.compare(column.name(), startColumn) >= 0;
                if (outOfBounds)
                    break;
            }
        }
        public boolean getNextBlock() throws IOException
        {
            return false;
        }
    }
}
