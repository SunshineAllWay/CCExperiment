package org.apache.cassandra.contrib.stress.operations;
import org.apache.cassandra.contrib.stress.util.OperationThread;
import org.apache.cassandra.db.ColumnFamilyType;
import org.apache.cassandra.thrift.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
public class RangeSlicer extends OperationThread
{
    public RangeSlicer(int index)
    {
        super(index);
    }
    public void run()
    {
        String format = "%0" + session.getTotalKeysLength() + "d";
        int current = range.begins();
        int limit   = range.limit();
        int count   = session.getColumnsPerKey();
        int last    = current + session.getKeysPerCall();
        SlicePredicate predicate = new SlicePredicate().setSlice_range(new SliceRange(ByteBuffer.wrap(new byte[] {}),
                                                                                      ByteBuffer.wrap(new byte[] {}),
                                                                                      false, count));
        if (session.getColumnFamilyType() == ColumnFamilyType.Super)
        {
            while (current < limit)
            {
                byte[] start = String.format(format, current).getBytes();
                byte[] end   = String.format(format, last).getBytes();
                List<KeySlice> slices = new ArrayList<KeySlice>();
                KeyRange range = new KeyRange(count).setStart_key(start).setEnd_key(end);
                for (int i = 0; i < session.getSuperColumns(); i++)
                {
                    String superColumnName = "S" + Integer.toString(i);
                    ColumnParent parent = new ColumnParent("Super1").setSuper_column(ByteBuffer.wrap(superColumnName.getBytes()));
                    long startTime = System.currentTimeMillis();
                    try
                    {
                        slices = client.get_range_slices(parent, predicate, range, session.getConsistencyLevel());
                        if (slices.size() == 0)
                        {
                            System.err.printf("Range %s->%s not found in Super Column %s.%n", new String(start), new String(end), superColumnName);
                            if (!session.ignoreErrors())
                                break;
                        }
                    }
                    catch (Exception e)
                    {
                        System.err.printf("Error while reading Super Column %s - %s%n", superColumnName, getExceptionMessage(e));
                        if (!session.ignoreErrors())
                            return;
                    }
                    session.operationCount.getAndIncrement(index);
                    session.latencies.getAndAdd(index, System.currentTimeMillis() - startTime);
                }
                current += slices.size() + 1;
                last = current + slices.size() + 1;
                session.keyCount.getAndAdd(index, slices.size());
            }
        }
        else
        {
            ColumnParent parent = new ColumnParent("Standard1");
            while (current < limit)
            {
                byte[] start = String.format(format, current).getBytes();
                byte[] end   = String.format(format, last).getBytes();
                List<KeySlice> slices = new ArrayList<KeySlice>();
                KeyRange range = new KeyRange(count).setStart_key(start).setEnd_key(end);
                long startTime = System.currentTimeMillis();
                try
                {
                    slices = client.get_range_slices(parent, predicate, range, session.getConsistencyLevel());
                    if (slices.size() == 0)
                    {
                        System.err.printf("Range %s->%s not found.%n", String.format(format, current), String.format(format, last));
                        if (!session.ignoreErrors())
                            break;
                    }
                }
                catch (Exception e)
                {
                    System.err.printf("Error while reading range %s->%s - %s%n", String.format(format, current), String.format(format, last), getExceptionMessage(e));
                    if (!session.ignoreErrors())
                        return;
                }
                current += slices.size() + 1;
                last = current + slices.size() + 1;
                session.operationCount.getAndIncrement(index);
                session.keyCount.getAndAdd(index, slices.size());
                session.latencies.getAndAdd(index, System.currentTimeMillis() - startTime);
            }
        }
    }
}
