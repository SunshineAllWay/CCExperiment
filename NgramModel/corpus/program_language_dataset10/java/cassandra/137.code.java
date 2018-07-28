package org.apache.cassandra.db;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.net.InetAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.db.filter.QueryPath;
import org.apache.cassandra.db.marshal.AbstractCommutativeType;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.io.ICompactSerializer;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.thrift.ConsistencyLevel;
public class CounterMutation implements IMutation
{
    private static final Logger logger = LoggerFactory.getLogger(CounterMutation.class);
    private static final CounterMutationSerializer serializer = new CounterMutationSerializer();
    private final RowMutation rowMutation;
    private final ConsistencyLevel consistency;
    public CounterMutation(RowMutation rowMutation, ConsistencyLevel consistency)
    {
        this.rowMutation = rowMutation;
        this.consistency = consistency;
    }
    public String getTable()
    {
        return rowMutation.getTable();
    }
    public ByteBuffer key()
    {
        return rowMutation.key();
    }
    public RowMutation rowMutation()
    {
        return rowMutation;
    }
    public ConsistencyLevel consistency()
    {
        return consistency;
    }
    public static CounterMutationSerializer serializer()
    {
        return serializer;
    }
    public RowMutation makeReplicationMutation() throws IOException
    {
        List<ReadCommand> readCommands = new LinkedList<ReadCommand>();
        for (ColumnFamily columnFamily : rowMutation.getColumnFamilies())
        {
            if (!columnFamily.metadata().getReplicateOnWrite())
                continue;
            if (!columnFamily.isSuper())
            {
                QueryPath queryPath = new QueryPath(columnFamily.metadata().cfName);
                ReadCommand readCommand = new SliceByNamesReadCommand(rowMutation.getTable(), rowMutation.key(), queryPath, columnFamily.getColumnNames());
                readCommands.add(readCommand);
                continue;
            }
            for (IColumn superColumn : columnFamily.getSortedColumns())
            {
                QueryPath queryPath = new QueryPath(columnFamily.metadata().cfName, superColumn.name());
                Collection<IColumn> subColumns = superColumn.getSubColumns();
                Collection<ByteBuffer> subColNames = new HashSet<ByteBuffer>(subColumns.size());
                for (IColumn subCol : subColumns)
                {
                    subColNames.add(subCol.name());
                }
                ReadCommand readCommand = new SliceByNamesReadCommand(rowMutation.getTable(), rowMutation.key(), queryPath, subColNames);
                readCommands.add(readCommand);
            }
        }
        List<InetAddress> foreignReplicas = StorageService.instance.getLiveNaturalEndpoints(rowMutation.getTable(), rowMutation.key());
        foreignReplicas.remove(FBUtilities.getLocalAddress()); 
        RowMutation replicationMutation = new RowMutation(rowMutation.getTable(), rowMutation.key());
        for (ReadCommand readCommand : readCommands)
        {
            Table table = Table.open(readCommand.table);
            Row row = readCommand.getRow(table);
            AbstractType defaultValidator = row.cf.metadata().getDefaultValidator();
            if (defaultValidator.isCommutative())
            {
                for (InetAddress foreignNode : foreignReplicas)
                {
                    ((AbstractCommutativeType)defaultValidator).cleanContext(row.cf, foreignNode);
                }
            }
            replicationMutation.add(row.cf);
        }
        return replicationMutation;
    }
    public Message makeMutationMessage() throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        serializer().serialize(this, dos);
        return new Message(FBUtilities.getLocalAddress(), StorageService.Verb.COUNTER_MUTATION, bos.toByteArray());
    }
    public boolean shouldReplicateOnWrite()
    {
        for (ColumnFamily cf : rowMutation.getColumnFamilies())
            if (cf.metadata().getReplicateOnWrite())
                return true;
        return false;
    }
    public void apply() throws IOException
    {
        rowMutation.updateCommutativeTypes(FBUtilities.getLocalAddress());
        rowMutation.deepCopy().apply();
    }
    @Override
    public String toString()
    {
        return toString(false);
    }
    public String toString(boolean shallow)
    {
        StringBuilder buff = new StringBuilder("CounterMutation(");
        buff.append(rowMutation.toString(shallow));
        buff.append(", ").append(consistency.toString());
        return buff.append(")").toString();
    }
}
class CounterMutationSerializer implements ICompactSerializer<CounterMutation>
{
    public void serialize(CounterMutation cm, DataOutputStream dos) throws IOException
    {
        RowMutation.serializer().serialize(cm.rowMutation(), dos);
        dos.writeUTF(cm.consistency().name());
    }
    public CounterMutation deserialize(DataInputStream dis) throws IOException
    {
        RowMutation rm = RowMutation.serializer().deserialize(dis);
        ConsistencyLevel consistency = Enum.valueOf(ConsistencyLevel.class, dis.readUTF());
        return new CounterMutation(rm, consistency);
    }
}
