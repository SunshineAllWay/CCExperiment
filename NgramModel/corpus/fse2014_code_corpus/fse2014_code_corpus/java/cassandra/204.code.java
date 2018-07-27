package org.apache.cassandra.db.marshal;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import org.apache.cassandra.db.*;
import org.apache.cassandra.utils.ByteBufferUtil;
public class CounterColumnType extends AbstractCommutativeType
{
    public static final CounterColumnType instance = new CounterColumnType();
    CounterColumnType() {} 
    public int compare(ByteBuffer o1, ByteBuffer o2)
    {
        if (o1.remaining() == 0)
        {
            return o2.remaining() == 0 ? 0 : -1;
        }
        if (o2.remaining() == 0)
        {
            return 1;
        }
        return ByteBufferUtil.compareUnsigned(o1, o2);
    }
    public String getString(ByteBuffer bytes)
    {
        if (bytes.remaining() == 0)
        {
            return "";
        }
        if (bytes.remaining() != 8)
        {
            throw new MarshalException("A long is exactly 8 bytes");
        }
        return String.valueOf(bytes.getLong(bytes.position()+bytes.arrayOffset()));
    }
    public Column createColumn(ByteBuffer name, ByteBuffer value, long timestamp)
    {
        return new CounterColumn(name, value, timestamp);
    }
    public void update(IColumnContainer cc, InetAddress node)
    {
        for (IColumn column : cc.getSortedColumns())
        {
            if (column instanceof SuperColumn)
            {
                update((IColumnContainer)column, node);
                continue;
            }
            if (column instanceof DeletedColumn)
                continue;
            ((CounterColumn)column).update(node);
        }
    }
    public void cleanContext(IColumnContainer cc, InetAddress node)
    {
        if ((cc instanceof ColumnFamily) && ((ColumnFamily)cc).isSuper())
        {
            for (IColumn column : cc.getSortedColumns())
            {
                SuperColumn supercol = (SuperColumn)column;
                cleanContext(supercol, node);
                if (0 == supercol.getSubColumns().size())
                    cc.remove(supercol.name());
            }
            return;
        }
        for (IColumn column : cc.getSortedColumns())
        {
            CounterColumn counterColumn = (CounterColumn)column;
            CounterColumn cleanedColumn = counterColumn.cleanNodeCounts(node);
            if (cleanedColumn == counterColumn)
                continue;
            cc.remove(counterColumn.name());
            if (null != cleanedColumn)
                cc.addColumn(cleanedColumn);
        }
    }
    public void validate(ByteBuffer bytes) throws MarshalException
    {
        if (bytes.remaining() != 8 && bytes.remaining() != 0)
            throw new MarshalException(String.format("Expected 8 or 0 byte long (%d)", bytes.remaining()));
    }
}
