package org.apache.cassandra.streaming;
import java.io.IOError;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.db.Table;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.io.sstable.Descriptor;
import org.apache.cassandra.io.sstable.SSTable;
import org.apache.cassandra.io.sstable.SSTableReader;
import org.apache.cassandra.utils.Pair;
public class StreamOut
{
    private static Logger logger = LoggerFactory.getLogger(StreamOut.class);
    public static void transferRanges(InetAddress target, String tableName, Collection<Range> ranges, Runnable callback, OperationType type)
    {
        assert ranges.size() > 0;
        StreamOutSession session = StreamOutSession.create(tableName, target, callback);
        logger.info("Beginning transfer to {}", target);
        logger.debug("Ranges are {}", StringUtils.join(ranges, ","));
        try
        {
            Table table = flushSSTable(tableName);
            transferSSTables(session, table.getAllSSTables(), ranges, type);
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
    }
    private static Table flushSSTable(String tableName) throws IOException
    {
        Table table = Table.open(tableName);
        logger.info("Flushing memtables for {}...", tableName);
        for (Future f : table.flush())
        {
            try
            {
                f.get();
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
            catch (ExecutionException e)
            {
                throw new RuntimeException(e);
            }
        }
        return table;
    }
    public static void transferRangesForRequest(StreamOutSession session, Collection<Range> ranges, OperationType type)
    {
        assert ranges.size() > 0;
        logger.info("Beginning transfer to {}", session.getHost());
        logger.debug("Ranges are {}", StringUtils.join(ranges, ","));
        try
        {
            Table table = flushSSTable(session.table);
            List<PendingFile> pending = createPendingFiles(table.getAllSSTables(), ranges, type);
            session.addFilesToStream(pending);
            session.begin();
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
    }
    public static void transferSSTables(StreamOutSession session, Collection<SSTableReader> sstables, Collection<Range> ranges, OperationType type) throws IOException
    {
        List<PendingFile> pending = createPendingFiles(sstables, ranges, type);
        if (pending.size() > 0)
        {
            session.addFilesToStream(pending);
            session.begin();
        }
        else
        {
            session.close();
        }
    }
    private static List<PendingFile> createPendingFiles(Collection<SSTableReader> sstables, Collection<Range> ranges, OperationType type)
    {
        List<PendingFile> pending = new ArrayList<PendingFile>();
        for (SSTableReader sstable : sstables)
        {
            Descriptor desc = sstable.descriptor;
            List<Pair<Long,Long>> sections = sstable.getPositionsForRanges(ranges);
            if (sections.isEmpty())
                continue;
            pending.add(new PendingFile(sstable, desc, SSTable.COMPONENT_DATA, sections, type));
        }
        logger.info("Stream context metadata {}, {} sstables.", pending, sstables.size());
        return pending;
    }
}
