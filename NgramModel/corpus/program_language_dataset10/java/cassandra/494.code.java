package org.apache.cassandra.dht;
import static org.junit.Assert.assertEquals;
import org.apache.cassandra.db.DecoratedKey;
import org.junit.Test;
public class RandomPartitionerTest extends PartitionerTestCase<BigIntegerToken>
{
    public void initPartitioner()
    {
        partitioner = new RandomPartitioner();
    }
}
