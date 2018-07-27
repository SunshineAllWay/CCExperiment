package org.apache.cassandra.db.commitlog;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.RowMutation;
import org.apache.cassandra.io.util.BufferedRandomAccessFile;
public class CommitLogSegment
{
    private static final Logger logger = LoggerFactory.getLogger(CommitLogSegment.class);
    private final BufferedRandomAccessFile logWriter;
    private final CommitLogHeader header;
    public CommitLogSegment()
    {
        this.header = new CommitLogHeader();
        String logFile = DatabaseDescriptor.getCommitLogLocation() + File.separator + "CommitLog-" + System.currentTimeMillis() + ".log";
        logger.info("Creating new commitlog segment " + logFile);
        try
        {
            logWriter = createWriter(logFile);
            writeHeader();
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
    }
    public static boolean possibleCommitLogFile(String filename)
    {
        return filename.matches("CommitLog-\\d+.log");
    }
    public void writeHeader() throws IOException
    {
        CommitLogHeader.writeCommitLogHeader(header, getHeaderPath());
    }
    private static BufferedRandomAccessFile createWriter(String file) throws IOException
    {
        return new BufferedRandomAccessFile(new File(file), "rw", 128 * 1024, true);
    }
    public CommitLogSegment.CommitLogContext write(RowMutation rowMutation) throws IOException
    {
        long currentPosition = -1L;
        try
        {
            currentPosition = logWriter.getFilePointer();
            CommitLogSegment.CommitLogContext cLogCtx = new CommitLogSegment.CommitLogContext(currentPosition);
            for (ColumnFamily columnFamily : rowMutation.getColumnFamilies())
            {
                CFMetaData cfm = DatabaseDescriptor.getCFMetaData(columnFamily.id());
                if (cfm == null)
                {
                    logger.error("Attempted to write commit log entry for unrecognized column family: " + columnFamily.id());
                }
                else
                {
                    Integer id = cfm.cfId;
                    if (!header.isDirty(id))
                    {
                        header.turnOn(id, logWriter.getFilePointer());
                        writeHeader();
                    }
                }
            }
            Checksum checksum = new CRC32();
            byte[] serializedRow = rowMutation.getSerializedBuffer();
            checksum.update(serializedRow.length);
            logWriter.writeInt(serializedRow.length);
            logWriter.writeLong(checksum.getValue());
            logWriter.write(serializedRow);
            checksum.update(serializedRow, 0, serializedRow.length);
            logWriter.writeLong(checksum.getValue());
            return cLogCtx;
        }
        catch (IOException e)
        {
            if (currentPosition != -1)
                logWriter.seek(currentPosition);
            throw e;
        }
    }
    public void sync() throws IOException
    {
        logWriter.sync();
    }
    public CommitLogContext getContext()
    {
        return new CommitLogContext(logWriter.getFilePointer());
    }
    public CommitLogHeader getHeader()
    {
        return header;
    }
    public String getPath()
    {
        return logWriter.getPath();
    }
    public String getHeaderPath()
    {
        return CommitLogHeader.getHeaderPathFromSegment(this);
    }
    public long length()
    {
        try
        {
            return logWriter.length();
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
    }
    public void close()
    {
        try
        {
            logWriter.close();
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
    }
    @Override
    public String toString()
    {
        return "CommitLogSegment(" + logWriter.getPath() + ')';
    }
    public class CommitLogContext
    {
        public final long position;
        public CommitLogContext(long position)
        {
            assert position >= 0;
            this.position = position;
        }
        public CommitLogSegment getSegment()
        {
            return CommitLogSegment.this;
        }
        @Override
        public String toString()
        {
            return "CommitLogContext(" +
                   "file='" + logWriter.getPath() + '\'' +
                   ", position=" + position +
                   ')';
        }
    }
}
