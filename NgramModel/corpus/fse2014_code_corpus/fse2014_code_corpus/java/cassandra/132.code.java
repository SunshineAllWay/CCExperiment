package org.apache.cassandra.db;
import java.io.DataOutput;
import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.io.sstable.IndexHelper;
import org.apache.cassandra.io.util.DataOutputBuffer;
import org.apache.cassandra.io.util.IIterableColumns;
import org.apache.cassandra.utils.BloomFilter;
public class ColumnIndexer
{
    public static void serialize(IIterableColumns columns, DataOutput dos)
    {
        try
        {
            serializeInternal(columns, dos);
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
    }
    public static void serializeInternal(IIterableColumns columns, DataOutput dos) throws IOException
    {
        int columnCount = columns.getEstimatedColumnCount();
        BloomFilter bf = BloomFilter.getFilter(columnCount, 4);
        if (columnCount == 0)
        {
            writeEmptyHeader(dos, bf);
            return;
        }
        List<IndexHelper.IndexInfo> indexList = new ArrayList<IndexHelper.IndexInfo>();
        int endPosition = 0, startPosition = -1;
        int indexSizeInBytes = 0;
        IColumn lastColumn = null, firstColumn = null;
        for (IColumn column : columns)
        {
            bf.add(column.name());
            if (firstColumn == null)
            {
                firstColumn = column;
                startPosition = endPosition;
            }
            endPosition += column.serializedSize();
            if (endPosition - startPosition >= DatabaseDescriptor.getColumnIndexSize())
            {
                IndexHelper.IndexInfo cIndexInfo = new IndexHelper.IndexInfo(firstColumn.name(), column.name(), startPosition, endPosition - startPosition);
                indexList.add(cIndexInfo);
                indexSizeInBytes += cIndexInfo.serializedSize();
                firstColumn = null;
            }
            lastColumn = column;
        }
        if (lastColumn == null)
        {
            writeEmptyHeader(dos, bf);
            return;
        }
        if (indexList.isEmpty() || columns.getComparator().compare(indexList.get(indexList.size() - 1).lastName, lastColumn.name()) != 0)
        {
            IndexHelper.IndexInfo cIndexInfo = new IndexHelper.IndexInfo(firstColumn.name(), lastColumn.name(), startPosition, endPosition - startPosition);
            indexList.add(cIndexInfo);
            indexSizeInBytes += cIndexInfo.serializedSize();
        }
        writeBloomFilter(dos, bf);
        assert indexSizeInBytes > 0;
        if (indexList.size() > 1)
        {
            dos.writeInt(indexSizeInBytes);
            for (IndexHelper.IndexInfo cIndexInfo : indexList)
            {
                cIndexInfo.serialize(dos);
            }
        }
        else
        {
            dos.writeInt(0);
        }
	}
    private static void writeEmptyHeader(DataOutput dos, BloomFilter bf)
            throws IOException
    {
        writeBloomFilter(dos, bf);
        dos.writeInt(0);
    }
    private static void writeBloomFilter(DataOutput dos, BloomFilter bf) throws IOException
    {
        DataOutputBuffer bufOut = new DataOutputBuffer();
        BloomFilter.serializer().serialize(bf, bufOut);
        dos.writeInt(bufOut.getLength());
        dos.write(bufOut.getData(), 0, bufOut.getLength());
    }
}
