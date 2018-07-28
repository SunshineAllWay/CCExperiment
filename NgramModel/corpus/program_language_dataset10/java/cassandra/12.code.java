package org.apache.cassandra.contrib.stress.operations;
import org.apache.cassandra.contrib.stress.util.OperationThread;
import org.apache.cassandra.db.ColumnFamilyType;
import org.apache.cassandra.thrift.*;
import org.apache.cassandra.utils.ByteBufferUtil;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class Inserter extends OperationThread
{
    public Inserter(int index)
    {
        super(index);
    }
    public void run()
    {
        List<String> values  = generateValues();
        List<Column> columns = new ArrayList<Column>();
        List<SuperColumn> superColumns = new ArrayList<SuperColumn>();
        String format = "%0" + session.getTotalKeysLength() + "d";
        for (int i = 0; i < session.getColumnsPerKey(); i++)
        {
            byte[] columnName = ("C" + Integer.toString(i)).getBytes();
            columns.add(new Column(ByteBuffer.wrap(columnName), ByteBuffer.wrap(new byte[] {}), System.currentTimeMillis()));
        }
        if (session.getColumnFamilyType() == ColumnFamilyType.Super)
        {
            for (int i = 0; i < session.getSuperColumns(); i++)
            {
                String superColumnName = "S" + Integer.toString(i);
                superColumns.add(new SuperColumn(ByteBuffer.wrap(superColumnName.getBytes()), columns));
            }
        }
        for (int i : range)
        {
            ByteBuffer key = ByteBuffer.wrap(String.format(format, i).getBytes());
            Map<ByteBuffer, Map<String, List<Mutation>>> record = new HashMap<ByteBuffer, Map<String, List<Mutation>>>();
            record.put(key, session.getColumnFamilyType() == ColumnFamilyType.Super
                                                          ? getSuperColumnsMutationMap(superColumns)
                                                          : getColumnsMutationMap(columns));
            String value = values.get(i % values.size());
            for (Column c : columns)
                c.value = ByteBuffer.wrap(value.getBytes());
            long start = System.currentTimeMillis();
            try
            {
                client.batch_mutate(record, session.getConsistencyLevel());
            }
            catch (Exception e)
            {
                System.err.printf("Error while inserting key %s - %s%n", ByteBufferUtil.string(key), getExceptionMessage(e));
                if (!session.ignoreErrors())
                    return;
            }
            session.operationCount.getAndIncrement(index);
            session.keyCount.getAndIncrement(index);
            session.latencies.getAndAdd(index, System.currentTimeMillis() - start);
        }
    }
    private Map<String, List<Mutation>> getSuperColumnsMutationMap(List<SuperColumn> superColumns)
    {
        List<Mutation> mutations = new ArrayList<Mutation>();
        Map<String, List<Mutation>> mutationMap = new HashMap<String, List<Mutation>>();
        for (SuperColumn s : superColumns)
        {
            ColumnOrSuperColumn superColumn = new ColumnOrSuperColumn().setSuper_column(s);
            mutations.add(new Mutation().setColumn_or_supercolumn(superColumn));
        }
        mutationMap.put("Super1", mutations);
        return mutationMap;
    }
    private Map<String, List<Mutation>> getColumnsMutationMap(List<Column> columns)
    {
        List<Mutation> mutations = new ArrayList<Mutation>();
        Map<String, List<Mutation>> mutationMap = new HashMap<String, List<Mutation>>();
        for (Column c : columns)
        {
            ColumnOrSuperColumn column = new ColumnOrSuperColumn().setColumn(c);
            mutations.add(new Mutation().setColumn_or_supercolumn(column));
        }
        mutationMap.put("Standard1", mutations);
        return mutationMap;
    }
}
