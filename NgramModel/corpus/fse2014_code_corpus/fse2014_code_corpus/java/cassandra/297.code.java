package org.apache.cassandra.io.util;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
public class MappedFileDataInput extends AbstractDataInput implements FileDataInput
{
    private final MappedByteBuffer buffer;
    private final String filename;
    private int position;
    public MappedFileDataInput(FileInputStream stream, String filename, int position) throws IOException
    {
        FileChannel channel = stream.getChannel();
        buffer = channel.map(FileChannel.MapMode.READ_ONLY, position, channel.size());
        this.filename = filename;
        this.position = position;
    }
    public MappedFileDataInput(MappedByteBuffer buffer, String filename, int position)
    {
        assert buffer != null;
        this.buffer = buffer;
        this.filename = filename;
        this.position = position;
    }
    protected void seekInternal(int pos)
    {
        position = pos;
    }
    @Override
    protected int getPosition()
    {
        return position;
    }
    @Override
    public boolean markSupported()
    {
        return false;
    }
    public void reset(FileMark mark) throws IOException
    {
        assert mark instanceof MappedFileDataInputMark;
        seekInternal(((MappedFileDataInputMark) mark).position);
    }
    public FileMark mark()
    {
        return new MappedFileDataInputMark(position);
    }
    public int bytesPastMark(FileMark mark)
    {
        assert mark instanceof MappedFileDataInputMark;
        assert position >= ((MappedFileDataInputMark) mark).position;
        return position - ((MappedFileDataInputMark) mark).position;
    }
    public boolean isEOF() throws IOException
    {
        return position == buffer.capacity();
    }
    public long bytesRemaining() throws IOException
    {
        return buffer.capacity() - position;
    }
    public String getPath()
    {
        return filename;
    }
    public int read() throws IOException
    {
        if (isEOF())
            return -1;
        return buffer.get(position++) & 0xFF;
    }
    public synchronized ByteBuffer readBytes(int length) throws IOException
    {
        int remaining = buffer.remaining() - position;
        assert length <= remaining
                : String.format("mmap segment underflow; remaining is %d but %d requested", remaining, length);
        ByteBuffer bytes = buffer.slice();
        bytes.position(buffer.position() + position).limit(buffer.position() + position + length);
        position += length;
        return bytes;
    }
    @Override
    public final void readFully(byte[] buffer) throws IOException
    {
        throw new UnsupportedOperationException("use readBytes instead");
    }
    @Override
    public final void readFully(byte[] buffer, int offset, int count) throws IOException
    {
        throw new UnsupportedOperationException("use readBytes instead");
    }
    public int skipBytes(int n) throws IOException
    {
        assert n >= 0 : "skipping negative bytes is illegal: " + n;
        if (n == 0)
            return 0;
        int oldPosition = position;
        assert ((long)oldPosition) + n <= Integer.MAX_VALUE;
        position = Math.min(buffer.capacity(), position + n);
        return position - oldPosition;
    }
    private static class MappedFileDataInputMark implements FileMark
    {
        int position;
        MappedFileDataInputMark(int position)
        {
            this.position = position;
        }
    }
}
