package org.apache.cassandra.contrib.stress.operations;
import org.apache.cassandra.contrib.stress.util.OperationThread;
import org.apache.cassandra.thrift.*;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.FBUtilities;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
public class IndexedRangeSlicer extends OperationThread
{
    public IndexedRangeSlicer(int index)
    {
        super(index);
    }
    public void run()
    {
        String format = "%0" + session.getTotalKeysLength() + "d";
        SlicePredicate predicate = new SlicePredicate().setSlice_range(new SliceRange(ByteBuffer.wrap(new byte[]{}),
                                                                                      ByteBuffer.wrap(new byte[] {}),
                                                                                      false, session.getColumnsPerKey()));
        List<String> values = super.generateValues();
        ColumnParent parent = new ColumnParent("Standard1");
        int expectedPerValue = session.getNumKeys() / values.size();
        ByteBuffer columnName = ByteBuffer.wrap("C1".getBytes());
        for (int i = range.begins(); i < range.size(); i++)
        {
            int received = 0;
            String startOffset = "0";
            ByteBuffer value = ByteBuffer.wrap(values.get(i % values.size()).getBytes());
            IndexExpression expression = new IndexExpression(columnName, IndexOperator.EQ, value);
            while (received < expectedPerValue)
            {
                IndexClause clause = new IndexClause(Arrays.asList(expression), ByteBuffer.wrap(startOffset.getBytes()),
                                                                                session.getKeysPerCall());
                List<KeySlice> results = null;
                long start = System.currentTimeMillis();
                try
                {
                    results = client.get_indexed_slices(parent, clause, predicate, session.getConsistencyLevel());
                    if (results.size() == 0)
                    {
                        System.err.printf("No indexed values from offset received: %s%n", startOffset);
                        if (!session.ignoreErrors())
                            break;
                    }
                }
                catch (Exception e)
                {
                    System.err.printf("Error on get_indexed_slices call for offset  %s - %s%n", startOffset, getExceptionMessage(e));
                    if (!session.ignoreErrors())
                        return;
                }
                received += results.size();
                startOffset = String.format(format, (1 + getMaxKey(results)));
                session.operationCount.getAndIncrement(index);
                session.keyCount.getAndAdd(index, results.size());
                session.latencies.getAndAdd(index, System.currentTimeMillis() - start);
            }
        }
    }
    private int getMaxKey(List<KeySlice> keySlices)
    {
        byte[] firstKey = keySlices.get(0).getKey();
        int maxKey = ByteBufferUtil.toInt(ByteBuffer.wrap(firstKey));
        for (KeySlice k : keySlices)
        {
            int currentKey = ByteBufferUtil.toInt(ByteBuffer.wrap(k.getKey()));
            if (currentKey > maxKey)
            {
                maxKey = currentKey;
            }
        }
        return maxKey;
    }
}
