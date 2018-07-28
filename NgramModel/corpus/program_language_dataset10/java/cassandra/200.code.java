package org.apache.cassandra.db.marshal;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import org.apache.cassandra.db.Column;
import org.apache.cassandra.db.IColumnContainer;
public abstract class AbstractCommutativeType extends AbstractType
{
    public boolean isCommutative()
    {
        return true;
    }
    public abstract Column createColumn(ByteBuffer name, ByteBuffer value, long timestamp);
    public abstract void update(IColumnContainer cc, InetAddress node);
    public abstract void cleanContext(IColumnContainer cc, InetAddress node);
}
