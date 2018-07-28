package org.apache.cassandra.io;
import java.io.DataOutput;
import java.io.IOError;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.io.sstable.SSTable;
import org.apache.cassandra.io.sstable.SSTableIdentityIterator;
import org.apache.cassandra.io.util.DataOutputBuffer;
public class PrecompactedRow extends AbstractCompactedRow
{
    private static Logger logger = LoggerFactory.getLogger(PrecompactedRow.class);
    private final DataOutputBuffer buffer;
    private int columnCount = 0;
    public PrecompactedRow(DecoratedKey key, DataOutputBuffer buffer)
    {
        super(key);
        this.buffer = buffer;
    }
    public PrecompactedRow(ColumnFamilyStore cfStore, List<SSTableIdentityIterator> rows, boolean major, int gcBefore)
    {
        super(rows.get(0).getKey());
        buffer = new DataOutputBuffer();
        Set<SSTable> sstables = new HashSet<SSTable>();
        for (SSTableIdentityIterator row : rows)
        {
            sstables.add(row.sstable);
        }
        boolean shouldPurge = major || !cfStore.isKeyInRemainingSSTables(key, sstables);
        if (rows.size() > 1 || shouldPurge)
        {
            ColumnFamily cf = null;
            for (SSTableIdentityIterator row : rows)
            {
                ColumnFamily thisCF;
                try
                {
                    thisCF = row.getColumnFamilyWithColumns();
                }
                catch (IOException e)
                {
                    logger.error("Skipping row " + key + " in " + row.getPath(), e);
                    continue;
                }
                if (cf == null)
                {
                    cf = thisCF;
                }
                else
                {
                    cf.addAll(thisCF);
                }
            }
            ColumnFamily cfPurged = shouldPurge ? ColumnFamilyStore.removeDeleted(cf, gcBefore) : cf;
            if (cfPurged == null)
                return;
            columnCount = ColumnFamily.serializer().serializeWithIndexes(cfPurged, buffer);
        }
        else
        {
            assert rows.size() == 1;
            try
            {
                rows.get(0).echoData(buffer);
                columnCount = rows.get(0).columnCount;
            }
            catch (IOException e)
            {
                throw new IOError(e);
            }
        }
    }
    public void write(DataOutput out) throws IOException
    {
        out.writeLong(buffer.getLength());
        out.write(buffer.getData(), 0, buffer.getLength());
    }
    public void update(MessageDigest digest)
    {
        digest.update(buffer.getData(), 0, buffer.getLength());
    }
    public boolean isEmpty()
    {
        return buffer.getLength() == 0;
    }
    public int columnCount()
    {
        return columnCount;
    }
}
