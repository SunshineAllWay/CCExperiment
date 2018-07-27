package org.apache.cassandra.streaming;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.io.util.FileUtils;
import org.apache.cassandra.net.MessagingService;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.Pair;
import org.apache.cassandra.utils.WrappedRunnable;
public class FileStreamTask extends WrappedRunnable
{
    private static Logger logger = LoggerFactory.getLogger( FileStreamTask.class );
    public static final int CHUNK_SIZE = 10*1024*1024;
    public static final int MAX_CONNECT_ATTEMPTS = 8;
    protected final StreamHeader header;
    protected final InetAddress to;
    private SocketChannel channel;
    public FileStreamTask(StreamHeader header, InetAddress to)
    {
        this.header = header;
        this.to = to;
    }
    public void runMayThrow() throws IOException
    {
        try
        {
            connectAttempt();
            stream();
        }
        finally
        {
            try
            {
                close();
            }
            catch (IOException e)
            {
                if (logger.isDebugEnabled())
                    logger.debug("error closing socket", e);
            }
        }
        if (logger.isDebugEnabled())
            logger.debug("Done streaming " + header.file);
    }
    private void stream() throws IOException
    {
        ByteBuffer buffer = MessagingService.instance().constructStreamHeader(header, false);
        writeHeader(buffer);
        if (header.file == null)
            return;
        RandomAccessFile raf = new RandomAccessFile(new File(header.file.getFilename()), "r");
        try
        {
            FileChannel fc = raf.getChannel();
            for (Pair<Long, Long> section : header.file.sections)
            {
                long length = section.right - section.left;
                long bytesTransferred = 0;
                while (bytesTransferred < length)
                {
                    long lastWrite = write(fc, section, length, bytesTransferred);
                    bytesTransferred += lastWrite;
                    header.file.progress += lastWrite;
                }
                if (logger.isDebugEnabled())
                    logger.debug("Bytes transferred " + bytesTransferred + "/" + header.file.size);
            }
        }
        finally
        {
            FileUtils.closeQuietly(raf);
        }
    }
    protected long write(FileChannel fc, Pair<Long, Long> section, long length, long bytesTransferred) throws IOException
    {
        long toTransfer = Math.min(CHUNK_SIZE, length - bytesTransferred);
        return fc.transferTo(section.left + bytesTransferred, toTransfer, channel);
    }
    protected void writeHeader(ByteBuffer buffer) throws IOException
    {
        channel.write(buffer);
        assert buffer.remaining() == 0;
    }
    private void connectAttempt() throws IOException
    {
        bind();
        int attempts = 0;
        while (true)
        {
            try
            {
                connect();
                break;
            }
            catch (IOException e)
            {
                if (++attempts >= MAX_CONNECT_ATTEMPTS)
                    throw e;
                long waitms = DatabaseDescriptor.getRpcTimeout() * (long)Math.pow(2, attempts);
                logger.warn("Failed attempt " + attempts + " to connect to " + to + " to stream " + header.file + ". Retrying in " + waitms + " ms. (" + e + ")");
                try
                {
                    Thread.sleep(waitms);
                }
                catch (InterruptedException wtf)
                {
                    throw new RuntimeException(wtf);
                }
            }
        }
    }
    protected void bind() throws IOException
    {
        channel = SocketChannel.open();
        channel.socket().bind(new InetSocketAddress(FBUtilities.getLocalAddress(), 0));
    }
    protected void connect() throws IOException
    {
        channel.connect(new InetSocketAddress(to, DatabaseDescriptor.getStoragePort()));
    }
    protected void close() throws IOException
    {
        channel.close();
    }
}
