package org.apache.cassandra.db.commitlog;
import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.concurrent.Stage;
import org.apache.cassandra.concurrent.StageManager;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.Config;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.RowMutation;
import org.apache.cassandra.db.Table;
import org.apache.cassandra.db.UnserializableColumnFamilyException;
import org.apache.cassandra.io.DeletionService;
import org.apache.cassandra.io.util.BufferedRandomAccessFile;
import org.apache.cassandra.io.util.FileUtils;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.WrappedRunnable;
public class CommitLog
{
    private static final int MAX_OUTSTANDING_REPLAY_COUNT = 1024;
    private static volatile int SEGMENT_SIZE = 128*1024*1024; 
    static final Logger logger = LoggerFactory.getLogger(CommitLog.class);
    public static final CommitLog instance = new CommitLog();
    private final Deque<CommitLogSegment> segments = new ArrayDeque<CommitLogSegment>();
    public static void setSegmentSize(int size)
    {
        SEGMENT_SIZE = size;
    }
    private final ICommitLogExecutorService executor;
    private CommitLog()
    {
        try
        {
            DatabaseDescriptor.createAllDirectories();
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
        segments.add(new CommitLogSegment());
        if (DatabaseDescriptor.getCommitLogSync() == Config.CommitLogSync.batch)
        {
            executor = new BatchCommitLogExecutorService();
        }
        else
        {
            executor = new PeriodicCommitLogExecutorService();
            final Callable syncer = new Callable()
            {
                public Object call() throws Exception
                {
                    sync();
                    return null;
                }
            };
            new Thread(new Runnable()
            {
                public void run()
                {
                    while (true)
                    {
                        try
                        {
                            executor.submit(syncer).get();
                            Thread.sleep(DatabaseDescriptor.getCommitLogSyncPeriod());
                        }
                        catch (InterruptedException e)
                        {
                            throw new AssertionError(e);
                        }
                        catch (ExecutionException e)
                        {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }, "PERIODIC-COMMIT-LOG-SYNCER").start();
        }
    }
    public void resetUnsafe()
    {
        segments.clear();
        segments.add(new CommitLogSegment());
    }
    private boolean manages(String name)
    {
        for (CommitLogSegment segment : segments)
        {
            if (segment.getPath().endsWith(name))
                return true;
        }
        return false;
    }
    public static void recover() throws IOException
    {
        String directory = DatabaseDescriptor.getCommitLogLocation();
        File[] files = new File(directory).listFiles(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return CommitLogSegment.possibleCommitLogFile(name) && !instance.manages(name);
            }
        });
        if (files.length == 0)
        {
            logger.info("No commitlog files found; skipping replay");
            return;
        }
        Arrays.sort(files, new FileUtils.FileComparator());
        logger.info("Replaying " + StringUtils.join(files, ", "));
        recover(files);
        for (File f : files)
        {
            FileUtils.delete(CommitLogHeader.getHeaderPathFromSegmentPath(f.getAbsolutePath())); 
            if (!f.delete())
                logger.error("Unable to remove " + f + "; you should remove it manually or next restart will replay it again (harmless, but time-consuming)");
        }
        logger.info("Log replay complete");
    }
    public static void recover(File[] clogs) throws IOException
    {
        Set<Table> tablesRecovered = new HashSet<Table>();
        List<Future<?>> futures = new ArrayList<Future<?>>();
        byte[] bytes = new byte[4096];
        Map<Integer, AtomicInteger> invalidMutations = new HashMap<Integer, AtomicInteger>();
        for (File file : clogs)
        {
            int bufferSize = (int)Math.min(file.length(), 32 * 1024 * 1024);
            BufferedRandomAccessFile reader = new BufferedRandomAccessFile(new File(file.getAbsolutePath()), "r", bufferSize, true);
            try
            {
                CommitLogHeader clHeader = null;
                int replayPosition = 0;
                String headerPath = CommitLogHeader.getHeaderPathFromSegmentPath(file.getAbsolutePath());
                try
                {
                    clHeader = CommitLogHeader.readCommitLogHeader(headerPath);
                    replayPosition = clHeader.getReplayPosition();
                }
                catch (IOException ioe)
                {
                    logger.info(headerPath + " incomplete, missing or corrupt.  Everything is ok, don't panic.  CommitLog will be replayed from the beginning");
                    logger.debug("exception was", ioe);
                }
                if (replayPosition < 0)
                {
                    logger.debug("skipping replay of fully-flushed {}", file);
                    continue;
                }
                reader.seek(replayPosition);
                if (logger.isDebugEnabled())
                    logger.debug("Replaying " + file + " starting at " + reader.getFilePointer());
                while (!reader.isEOF())
                {
                    if (logger.isDebugEnabled())
                        logger.debug("Reading mutation at " + reader.getFilePointer());
                    long claimedCRC32;
                    Checksum checksum = new CRC32();
                    int serializedSize;
                    try
                    {
                        serializedSize = reader.readInt();
                        long claimedSizeChecksum = reader.readLong();
                        checksum.update(serializedSize);
                        if (checksum.getValue() != claimedSizeChecksum || serializedSize <= 0)
                            break; 
                        if (serializedSize > bytes.length)
                            bytes = new byte[(int) (1.2 * serializedSize)];
                        reader.readFully(bytes, 0, serializedSize);
                        claimedCRC32 = reader.readLong();
                    }
                    catch(EOFException eof)
                    {
                        break; 
                    }
                    checksum.update(bytes, 0, serializedSize);
                    if (claimedCRC32 != checksum.getValue())
                    {
                        continue;
                    }
                    ByteArrayInputStream bufIn = new ByteArrayInputStream(bytes, 0, serializedSize);
                    RowMutation rm = null;
                    try
                    {
                        rm = RowMutation.serializer().deserialize(new DataInputStream(bufIn));
                    }
                    catch (UnserializableColumnFamilyException ex)
                    {
                        AtomicInteger i = invalidMutations.get(ex.cfId);
                        if (i == null)
                        {
                            i = new AtomicInteger(1);
                            invalidMutations.put(ex.cfId, i);
                        }
                        else
                            i.incrementAndGet();
                        continue;
                    }
                    if (logger.isDebugEnabled())
                        logger.debug(String.format("replaying mutation for %s.%s: %s",
                                                    rm.getTable(),
                                                    ByteBufferUtil.bytesToHex(rm.key()),
                                                    "{" + StringUtils.join(rm.getColumnFamilies(), ", ") + "}"));
                    final Table table = Table.open(rm.getTable());
                    tablesRecovered.add(table);
                    final Collection<ColumnFamily> columnFamilies = new ArrayList<ColumnFamily>(rm.getColumnFamilies());
                    final long entryLocation = reader.getFilePointer();
                    final CommitLogHeader finalHeader = clHeader;
                    final RowMutation frm = rm;
                    Runnable runnable = new WrappedRunnable()
                    {
                        public void runMayThrow() throws IOException
                        {
                            RowMutation newRm = new RowMutation(frm.getTable(), frm.key());
                            for (ColumnFamily columnFamily : columnFamilies)
                            {
                                if (CFMetaData.getCF(columnFamily.id()) == null)
                                    continue;
                                if (finalHeader == null || (finalHeader.isDirty(columnFamily.id()) && entryLocation > finalHeader.getPosition(columnFamily.id())))
                                    newRm.add(columnFamily);
                            }
                            if (!newRm.isEmpty())
                            {
                                Table.open(newRm.getTable()).apply(newRm, false);
                            }
                        }
                    };
                    futures.add(StageManager.getStage(Stage.MUTATION).submit(runnable));
                    if (futures.size() > MAX_OUTSTANDING_REPLAY_COUNT)
                    {
                        FBUtilities.waitOnFutures(futures);
                        futures.clear();
                    }
                }
            }
            finally
            {
                FileUtils.closeQuietly(reader);
                logger.info("Finished reading " + file);
            }
        }
        for (Map.Entry<Integer, AtomicInteger> entry : invalidMutations.entrySet())
            logger.info(String.format("Skipped %d mutations from unknown (probably removed) CF with id %d", entry.getValue().intValue(), entry.getKey()));
        FBUtilities.waitOnFutures(futures);
        logger.debug("Finished waiting on mutations from recovery");
        futures.clear();
        for (Table table : tablesRecovered)
            futures.addAll(table.flush());
        FBUtilities.waitOnFutures(futures);
    }
    private CommitLogSegment currentSegment()
    {
        return segments.getLast();
    }
    public CommitLogSegment.CommitLogContext getContext()
    {
        Callable<CommitLogSegment.CommitLogContext> task = new Callable<CommitLogSegment.CommitLogContext>()
        {
            public CommitLogSegment.CommitLogContext call() throws Exception
            {
                return currentSegment().getContext();
            }
        };
        try
        {
            return executor.submit(task).get();
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
    public void add(RowMutation rowMutation) throws IOException
    {
        executor.add(new LogRecordAdder(rowMutation));
    }
    public void discardCompletedSegments(final Integer cfId, final CommitLogSegment.CommitLogContext context) throws IOException
    {
        Callable task = new Callable()
        {
            public Object call() throws IOException
            {
                discardCompletedSegmentsInternal(context, cfId);
                return null;
            }
        };
        try
        {
            executor.submit(task).get();
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
    private void discardCompletedSegmentsInternal(CommitLogSegment.CommitLogContext context, Integer id) throws IOException
    {
        if (logger.isDebugEnabled())
            logger.debug("discard completed log segments for " + context + ", column family " + id + ".");
        Iterator<CommitLogSegment> iter = segments.iterator();
        while (iter.hasNext())
        {
            CommitLogSegment segment = iter.next();
            CommitLogHeader header = segment.getHeader();
            if (segment.equals(context.getSegment()))
            {
                if (logger.isDebugEnabled())
                    logger.debug("Marking replay position " + context.position + " on commit log " + segment);
                header.turnOn(id, context.position);
                segment.writeHeader();
                break;
            }
            header.turnOff(id);
            if (header.isSafeToDelete() && iter.hasNext())
            {
                logger.info("Discarding obsolete commit log:" + segment);
                segment.close();
                DeletionService.submitDelete(segment.getHeaderPath());
                DeletionService.submitDelete(segment.getPath());
                iter.remove();
            }
            else
            {
                if (logger.isDebugEnabled())
                    logger.debug("Not safe to delete commit log " + segment + "; dirty is " + header.dirtyString());
                segment.writeHeader();
            }
        }
    }
    void sync() throws IOException
    {
        currentSegment().sync();
    }
    class LogRecordAdder implements Callable, Runnable
    {
        final RowMutation rowMutation;
        LogRecordAdder(RowMutation rm)
        {
            this.rowMutation = rm;
        }
        public void run()
        {
            try
            {
                currentSegment().write(rowMutation);
                if (currentSegment().length() >= SEGMENT_SIZE)
                {
                    sync();
                    segments.add(new CommitLogSegment());
                }
            }
            catch (IOException e)
            {
                throw new IOError(e);
            }
        }
        public Object call() throws Exception
        {
            run();
            return null;
        }
    }
}
