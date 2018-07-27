package org.apache.cassandra.io.util;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import org.apache.cassandra.utils.CLibrary;
public class BufferedRandomAccessFile extends RandomAccessFile implements FileDataInput
{
    private static final long MAX_BYTES_IN_PAGE_CACHE = (long) Math.pow(2, 27); 
    private final String filePath;
    public static final int DEFAULT_BUFFER_SIZE = 65535;
    private boolean isDirty, syncNeeded, hitEOF = false;
    private ByteBuffer buffer;
    private long bufferOffset, bufferEnd, current = 0;
    private long maxBufferSize;
    private final long fileLength;
    private final FileChannel channel;
    private long markedPointer;
    private int fd;
    private final boolean skipCache;
    private long bytesSinceCacheFlush = 0;
    private long minBufferOffset = Long.MAX_VALUE;
    public BufferedRandomAccessFile(String name, String mode) throws IOException
    {
        this(new File(name), mode, 0);
    }
    public BufferedRandomAccessFile(String name, String mode, int bufferSize) throws IOException
    {
        this(new File(name), mode, bufferSize);
    }
    public BufferedRandomAccessFile(File file, String mode) throws IOException
    {
        this(file, mode, 0);
    }
    public BufferedRandomAccessFile(File file, String mode, int bufferSize) throws IOException
    {
        this(file, mode, bufferSize, false);
    }
    public BufferedRandomAccessFile(File file, String mode, int bufferSize, boolean skipCache) throws IOException
    {
        super(file, mode);
        this.skipCache = skipCache;
        channel = super.getChannel();
        filePath = file.getAbsolutePath();
        maxBufferSize = Math.max(bufferSize, DEFAULT_BUFFER_SIZE);
        buffer = ByteBuffer.allocate((int) maxBufferSize);
        fileLength = (mode.equals("r")) ? this.channel.size() : -1;
        bufferEnd = reBuffer(); 
        fd = CLibrary.getfd(this.getFD());
    }
    public void sync() throws IOException
    {
        if (syncNeeded)
        {
            flush();
            channel.force(true); 
            if (skipCache)
            {
                CLibrary.trySkipCache(this.fd, 0, 0);
                minBufferOffset = Long.MAX_VALUE;
                bytesSinceCacheFlush = 0;
            }
            syncNeeded = false;
        }
    }
    public void flush() throws IOException
    {
        if (isDirty)
        {
            if (channel.position() != bufferOffset)
                channel.position(bufferOffset);
            int lengthToWrite = (int) (bufferEnd - bufferOffset);
            super.write(buffer.array(), 0, lengthToWrite);
            if (skipCache)
            {
                bytesSinceCacheFlush += lengthToWrite;
                if (bufferOffset < minBufferOffset)
                    minBufferOffset = bufferOffset;
                if (bytesSinceCacheFlush >= MAX_BYTES_IN_PAGE_CACHE)
                {
                    CLibrary.trySkipCache(this.fd, (int) minBufferOffset, 0);
                    minBufferOffset = bufferOffset;
                    bytesSinceCacheFlush = 0;
                }
            }
            isDirty = false;
        }
    }
    private long reBuffer() throws IOException
    {
        flush(); 
        buffer.clear();
        bufferOffset = current;
        if (bufferOffset > channel.size())
        {
            buffer.rewind();
            bufferEnd = bufferOffset;
            hitEOF = true;
            return 0;
        }
        if (bufferOffset < minBufferOffset)
            minBufferOffset = bufferOffset;
        channel.position(bufferOffset); 
        long bytesRead = channel.read(buffer); 
        hitEOF = (bytesRead < maxBufferSize); 
        bufferEnd = bufferOffset + bytesRead;
        buffer.rewind();
        bytesSinceCacheFlush += bytesRead;
        if (skipCache && bytesSinceCacheFlush >= MAX_BYTES_IN_PAGE_CACHE)
        {
            CLibrary.trySkipCache(this.fd, (int) minBufferOffset, 0);
            bytesSinceCacheFlush = 0;
            minBufferOffset = Long.MAX_VALUE;
        }
        return bytesRead;
    }
    @Override
    public int read() throws IOException
    {
        if (isEOF())
            return -1; 
        if (current < bufferOffset || current >= bufferEnd)
        {
            reBuffer();
            if (current == bufferEnd && hitEOF)
                return -1; 
        }
        byte result = buffer.get();
        current++;
        return ((int) result) & 0xFF;
    }
    @Override
    public int read(byte[] buffer) throws IOException
    {
        return read(buffer, 0, buffer.length);
    }
    @Override
    public int read(byte[] buff, int offset, int length) throws IOException
    {
        int bytesCount = 0;
        while (length > 0)
        {
            int bytesRead = readAtMost(buff, offset, length);
            if (bytesRead == -1)
                return -1; 
            offset += bytesRead;
            length -= bytesRead;
            bytesCount += bytesRead;
        }
        return bytesCount;
    }
    private int readAtMost(byte[] buff, int offset, int length) throws IOException
    {
        if (length >= bufferEnd && hitEOF)
            return -1;
        final int left = (int) maxBufferSize - buffer.position();
        if (current < bufferOffset || left < length)
        {
            reBuffer();
        }
        length = Math.min(length, (int) (maxBufferSize - buffer.position()));
        buffer.get(buff, offset, length);
        current += length;
        return length;
    }
    public ByteBuffer readBytes(int length) throws IOException
    {
        assert length >= 0 : "buffer length should not be negative: " + length;
        byte[] buff = new byte[length];
        readFully(buff); 
        return ByteBuffer.wrap(buff);
    }
    @Override
    public void write(int val) throws IOException
    {
        byte[] b = new byte[1];
        b[0] = (byte) val;
        this.write(b, 0, b.length);
    }
    @Override
    public void write(byte[] b) throws IOException
    {
        write(b, 0, b.length);
    }
    @Override
    public void write(byte[] buff, int offset, int length) throws IOException
    {
        while (length > 0)
        {
            int n = writeAtMost(buff, offset, length);
            offset += n;
            length -= n;
            isDirty = true;
            syncNeeded = true;
        }
    }
    private int writeAtMost(byte[] buff, int offset, int length) throws IOException
    {
        final int left = (int) maxBufferSize - buffer.position();
        if (current < bufferOffset || left < length)
        {
            reBuffer();
        }
        length = Math.min(length, (int) (maxBufferSize - buffer.position()));
        buffer.put(buff, offset, length);
        current += length;
        if (current > bufferEnd)
            bufferEnd = current;
        return length;
    }
    @Override
    public void seek(long newPosition) throws IOException
    {
        current = newPosition;
        if (newPosition >= bufferEnd || newPosition < bufferOffset)
        {
            reBuffer(); 
        }
        final int delta = (int) (newPosition - bufferOffset);
        buffer.position(delta);
    }
    @Override
    public int skipBytes(int count) throws IOException
    {
        if (count > 0)
        {
            long currentPos = getFilePointer(), eof = length();
            int newCount = (int) ((currentPos + count > eof) ? eof - currentPos : count);
            seek(currentPos + newCount);
            return newCount;
        }
        return 0;
    }
    public long length() throws IOException
    {
        return (fileLength == -1) ? Math.max(current, channel.size()) : fileLength;
    }
    public long getFilePointer()
    {
        return bufferOffset + buffer.position();
    }
    public String getPath()
    {
        return filePath;
    }
    public boolean isEOF() throws IOException
    {
        return getFilePointer() == length();
    }
    public long bytesRemaining() throws IOException
    {
        return length() - getFilePointer();
    }
    @Override
    public void close() throws IOException
    {
        sync();
        buffer = null;
        if (skipCache && bytesSinceCacheFlush > 0)
        {
            CLibrary.trySkipCache(this.fd, 0, 0);
        }
        super.close();
    }
    public void reset() throws IOException
    {
        seek(markedPointer);
    }
    public int bytesPastMark()
    {
        long bytes = getFilePointer() - markedPointer;
        assert bytes >= 0;
        if (bytes > Integer.MAX_VALUE)
            throw new UnsupportedOperationException("Overflow: " + bytes);
        return (int) bytes;
    }
    public FileMark mark()
    {
        markedPointer = getFilePointer();
        return new BufferedRandomAccessFileMark(markedPointer);
    }
    public void reset(FileMark mark) throws IOException
    {
        assert mark instanceof BufferedRandomAccessFileMark;
        seek(((BufferedRandomAccessFileMark) mark).pointer);
    }
    public int bytesPastMark(FileMark mark)
    {
        assert mark instanceof BufferedRandomAccessFileMark;
        long bytes = getFilePointer() - ((BufferedRandomAccessFileMark) mark).pointer;
        assert bytes >= 0;
        if (bytes > Integer.MAX_VALUE)
            throw new UnsupportedOperationException("Overflow: " + bytes);
        return (int) bytes;
    }
    protected static class BufferedRandomAccessFileMark implements FileMark
    {
        long pointer;
        public BufferedRandomAccessFileMark(long pointer)
        {
            this.pointer = pointer;
        }
    }
}
