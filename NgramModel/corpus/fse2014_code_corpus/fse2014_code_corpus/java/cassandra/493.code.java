package org.apache.cassandra.dht;
import java.nio.ByteBuffer;
import java.util.Random;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.apache.cassandra.db.DecoratedKey;
public abstract class PartitionerTestCase<T extends Token>
{
    protected IPartitioner<T> partitioner;
    public abstract void initPartitioner();
    @Before
    public void clean()
    {
        initPartitioner();
    }
    public T tok(byte[] key)
    {
        return partitioner.getToken(ByteBuffer.wrap(key));
    }
    public T tok(String key)
    {
        return tok(key.getBytes());
    }
    public void assertMidpoint(T left, T right, int depth)
    {
        Random rand = new Random();
        for (int i = 0; i < 1000; i++)
        {
            assertMidpoint(left, right, rand, depth);
        }
    }
    private void assertMidpoint(Token left, Token right, Random rand, int depth)
    {
        Token mid = partitioner.midpoint(left, right);
        assert new Range(left, right).contains(mid)
                : "For " + left + "," + right + ": range did not contain mid:" + mid;
        if (depth < 1)
            return;
        if (rand.nextBoolean())
            assertMidpoint(left, mid, rand, depth-1);
        else
            assertMidpoint(mid, right, rand, depth-1);
    }
    @Test
    public void testMidpoint()
    {
        assertMidpoint(tok("a"), tok("b"), 16);
        assertMidpoint(tok("a"), tok("bbb"), 16);
    }
    @Test
    public void testMidpointMinimum()
    {
        T mintoken = partitioner.getMinimumToken(); 
        assert mintoken.compareTo(partitioner.midpoint(mintoken, mintoken)) != 0;
        assertMidpoint(mintoken, tok("a"), 16);
        assertMidpoint(mintoken, tok("aaa"), 16);
        assertMidpoint(mintoken, mintoken, 126);
        assertMidpoint(tok("a"), mintoken, 16);
    }
    @Test
    public void testMidpointWrapping()
    {
    }
    @Test
    public void testTokenFactoryBytes()
    {
        Token.TokenFactory factory = partitioner.getTokenFactory();
        assert tok("a").compareTo(factory.fromByteArray(factory.toByteArray(tok("a")))) == 0;
    }
    @Test
    public void testTokenFactoryStrings()
    {
        Token.TokenFactory factory = partitioner.getTokenFactory();
        assert tok("a").compareTo(factory.fromString(factory.toString(tok("a")))) == 0;
    }
}
