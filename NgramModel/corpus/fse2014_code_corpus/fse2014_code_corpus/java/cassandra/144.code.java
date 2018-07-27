package org.apache.cassandra.db;
import java.nio.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.utils.ByteBufferUtil;
public class DeletedColumn extends Column
{
    private static Logger logger = LoggerFactory.getLogger(DeletedColumn.class);
    public DeletedColumn(ByteBuffer name, int localDeletionTime, long timestamp)
    {
        this(name, ByteBufferUtil.bytes(localDeletionTime), timestamp);
    }
    public DeletedColumn(ByteBuffer name, ByteBuffer value, long timestamp)
    {
        super(name, value, timestamp);
    }
    @Override
    public boolean isMarkedForDelete()
    {
        return true;
    }
    @Override
    public long getMarkedForDeleteAt()
    {
        return timestamp;
    }
    @Override
    public int getLocalDeletionTime()
    {
       return value.getInt(value.position());
    }
    @Override
    public IColumn reconcile(IColumn column)
    {
        if (column instanceof DeletedColumn)
            return super.reconcile(column);
        return column.reconcile(this);
    }
    @Override
    public IColumn deepCopy()
    {
        return new DeletedColumn(ByteBufferUtil.clone(name), ByteBufferUtil.clone(value), timestamp);
    }
}
