package org.apache.cassandra.io.util;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.IOException;
public final class OutputBuffer extends ByteArrayOutputStream
{
    public OutputBuffer()
    {
        this(128);
    }
    public OutputBuffer(int size)
    {
        super(size);
    }
    public byte[] getData()
    {
        return buf;
    }
    public int getLength()
    {
        return count;
    }
    public void write(DataInput in, int len) throws IOException
    {
        int newcount = count + len;
        if (newcount > buf.length)
        {
            byte newbuf[] = new byte[Math.max(buf.length << 1, newcount)];
            System.arraycopy(buf, 0, newbuf, 0, count);
            buf = newbuf;
        }
        in.readFully(buf, count, len);
        count = newcount;
    }
    public byte[] asByteArray()
    {
        if (count == buf.length)
            return buf;
        return this.toByteArray();
    }
}
