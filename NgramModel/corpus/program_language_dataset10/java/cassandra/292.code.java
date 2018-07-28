package org.apache.cassandra.io.util;
import java.io.DataOutputStream;
public final class DataOutputBuffer extends DataOutputStream
{
    public DataOutputBuffer()
    {
        this(128);
    }
    public DataOutputBuffer(int size)
    {
        super(new OutputBuffer(size));
    }
    private OutputBuffer buffer()
    {
        return (OutputBuffer)out;
    }
    public byte[] asByteArray()
    {
        return buffer().asByteArray();
    }
    public byte[] getData()
    {
        return buffer().getData();
    }
    public int getLength()
    {
        return buffer().getLength();
    }
    public DataOutputBuffer reset()
    {
        this.written = 0;
        buffer().reset();
        return this;
    }
}
