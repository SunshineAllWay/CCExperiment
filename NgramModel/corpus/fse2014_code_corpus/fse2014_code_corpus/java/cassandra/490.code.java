package org.apache.cassandra.dht;
import org.junit.Test;
import org.apache.cassandra.utils.FBUtilities;
public class ByteOrderedPartitionerTest extends PartitionerTestCase<BytesToken>
{
    @Override
    public void initPartitioner()
    {
        partitioner = new ByteOrderedPartitioner();
    }
}
