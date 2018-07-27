package org.apache.cassandra.db;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import org.apache.log4j.Logger;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.io.util.DataOutputBuffer;
import org.apache.cassandra.utils.ByteBufferUtil;
public class ExpiringColumn extends Column
{
    private static Logger logger = Logger.getLogger(ExpiringColumn.class);
    private final int localExpirationTime;
    private final int timeToLive;
    public ExpiringColumn(ByteBuffer name, ByteBuffer value, long timestamp, int timeToLive)
    {
      this(name, value, timestamp, timeToLive, (int) (System.currentTimeMillis() / 1000) + timeToLive);
    }
    public ExpiringColumn(ByteBuffer name, ByteBuffer value, long timestamp, int timeToLive, int localExpirationTime)
    {
        super(name, value, timestamp);
        assert timeToLive > 0 : timeToLive;
        assert localExpirationTime > 0 : localExpirationTime;
        this.timeToLive = timeToLive;
        this.localExpirationTime = localExpirationTime;
    }
    public int getTimeToLive()
    {
        return timeToLive;
    }
    @Override
    public boolean isMarkedForDelete()
    {
        return (int) (System.currentTimeMillis() / 1000 ) > localExpirationTime;
    }
    @Override
    public int size()
    {
        return super.size() + DBConstants.intSize_ + DBConstants.intSize_;
    }
    @Override
    public void updateDigest(MessageDigest digest)
    {
        digest.update(name.duplicate());
        digest.update(value.duplicate());
        DataOutputBuffer buffer = new DataOutputBuffer();
        try
        {
            buffer.writeLong(timestamp);
            buffer.writeByte(ColumnSerializer.EXPIRATION_MASK);
            buffer.writeInt(timeToLive);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        digest.update(buffer.getData(), 0, buffer.getLength());
    }
    @Override
    public int getLocalDeletionTime()
    {
        return localExpirationTime;
    }
    @Override
    public IColumn deepCopy()
    {
        return new ExpiringColumn(ByteBufferUtil.clone(name), ByteBufferUtil.clone(value), timestamp, timeToLive, localExpirationTime);
    }
    @Override
    public String getString(AbstractType comparator)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getString(comparator));
        sb.append("!");
        sb.append(timeToLive);
        return sb.toString();
    }
    @Override
    public long getMarkedForDeleteAt()
    {
        if (isMarkedForDelete())
        {
            return timestamp;
        }
        else
        {
            throw new IllegalStateException("column is not marked for delete");
        }
    }
}
