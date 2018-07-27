package org.apache.cassandra.db;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.io.ICompactSerializer2;
public class ColumnFamilySerializer implements ICompactSerializer2<ColumnFamily>
{
    private static final Logger logger = LoggerFactory.getLogger(ColumnFamilySerializer.class);
    public void serialize(ColumnFamily columnFamily, DataOutput dos)
    {
        try
        {
            if (columnFamily == null)
            {
                dos.writeBoolean(false);
                return;
            }
            dos.writeBoolean(true);
            dos.writeInt(columnFamily.id());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        serializeForSSTable(columnFamily, dos);
    }
    public int serializeForSSTable(ColumnFamily columnFamily, DataOutput dos)
    {
        try
        {
            serializeCFInfo(columnFamily, dos);
            Collection<IColumn> columns = columnFamily.getSortedColumns();
            int count = columns.size();
            dos.writeInt(count);
            for (IColumn column : columns)
            {
                columnFamily.getColumnSerializer().serialize(column, dos);
            }
            return count;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    public void serializeCFInfo(ColumnFamily columnFamily, DataOutput dos) throws IOException
    {
        dos.writeInt(columnFamily.localDeletionTime.get());
        dos.writeLong(columnFamily.markedForDeleteAt.get());
    }
    public int serializeWithIndexes(ColumnFamily columnFamily, DataOutput dos)
    {
        ColumnIndexer.serialize(columnFamily, dos);
        return serializeForSSTable(columnFamily, dos);
    }
    public ColumnFamily deserialize(DataInput dis) throws IOException
    {
        if (!dis.readBoolean())
            return null;
        int cfId = dis.readInt();
        if (CFMetaData.getCF(cfId) == null)
            throw new UnserializableColumnFamilyException("Couldn't find cfId=" + cfId, cfId);
        ColumnFamily cf = ColumnFamily.create(cfId);
        deserializeFromSSTableNoColumns(cf, dis);
        deserializeColumns(dis, cf);
        return cf;
    }
    public void deserializeColumns(DataInput dis, ColumnFamily cf) throws IOException
    {
        int size = dis.readInt();
        for (int i = 0; i < size; ++i)
        {
            IColumn column = cf.getColumnSerializer().deserialize(dis);
            cf.addColumn(column);
        }
    }
    public ColumnFamily deserializeFromSSTableNoColumns(ColumnFamily cf, DataInput input) throws IOException
    {        
        cf.delete(input.readInt(), input.readLong());
        return cf;
    }
}
