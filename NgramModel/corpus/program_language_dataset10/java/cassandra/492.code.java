package org.apache.cassandra.dht;
import java.math.BigInteger;
import org.junit.Before;
import org.junit.Test;
import org.apache.cassandra.utils.FBUtilities;
public class OrderPreservingPartitionerTest extends PartitionerTestCase<StringToken> {
    @Override
    public void initPartitioner()
    {
        partitioner = new OrderPreservingPartitioner();
    }
    @Test
    public void testCompare()
    {
        assert tok("").compareTo(tok("asdf")) < 0;
        assert tok("asdf").compareTo(tok("")) > 0;
        assert tok("").compareTo(tok("")) == 0;
        assert tok("z").compareTo(tok("a")) > 0;
        assert tok("a").compareTo(tok("z")) < 0;
        assert tok("asdf").compareTo(tok("asdf")) == 0;
        assert tok("asdz").compareTo(tok("asdf")) > 0;
    }
}
