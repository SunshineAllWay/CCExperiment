package org.apache.cassandra.service;
import java.net.InetAddress;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.apache.cassandra.CleanupHelper;
import static org.apache.cassandra.Util.range;
import static org.apache.cassandra.Util.bounds;
import static org.apache.cassandra.Util.token;
import org.apache.cassandra.dht.AbstractBounds;
import org.apache.cassandra.locator.TokenMetadata;
public class StorageProxyTest extends CleanupHelper
{
    @BeforeClass
    public static void beforeClass() throws Throwable
    {
        TokenMetadata tmd = StorageService.instance.getTokenMetadata();
        tmd.updateNormalToken(token("1"), InetAddress.getByName("127.0.0.1"));
        tmd.updateNormalToken(token("6"), InetAddress.getByName("127.0.0.6"));
    }
    private void testGRR(AbstractBounds queryRange, AbstractBounds... expected)
    {
        List<AbstractBounds> restricted = StorageProxy.getRestrictedRanges(queryRange);
        assertEquals(restricted.toString(), expected.length, restricted.size());
        for (int i = 0; i < expected.length; i++)
            assertEquals("Mismatch for index " + i + ": " + restricted, expected[i], restricted.get(i));
    }
    @Test
    public void testGRR() throws Throwable
    {
        testGRR(range("2", "5"), range("2", "5"));
        testGRR(bounds("2", "5"), bounds("2", "5"));
        testGRR(range("2", "7"), range("2", "6"), range("6", "7"));
        testGRR(bounds("2", "7"), bounds("2", "6"), range("6", "7"));
        testGRR(range("", "2"), range("", "1"), range("1", "2"));
        testGRR(bounds("", "2"), bounds("", "1"), range("1", "2"));
        testGRR(range("5", ""), range("5", "6"), range("6", ""));
        testGRR(bounds("5", ""), bounds("5", "6"), range("6", ""));
        testGRR(range("0", "7"), range("0", "1"), range("1", "6"), range("6", "7"));
        testGRR(bounds("0", "7"), bounds("0", "1"), range("1", "6"), range("6", "7"));
    }
    @Test
    public void testGRRExact() throws Throwable
    {
        testGRR(range("1", "5"), range("1", "5"));
        testGRR(bounds("1", "5"), bounds("1", "1"), range("1", "5"));
        testGRR(range("2", "6"), range("2", "6"));
        testGRR(bounds("2", "6"), bounds("2", "6"));
        testGRR(range("1", "6"), range("1", "6"));
        testGRR(bounds("1", "6"), bounds("1", "1"), range("1", "6"));
    }
    @Test
    public void testGRRWrapped() throws Throwable
    {
        testGRR(range("7", "0"), range("7", ""), range("", "0"));
        testGRR(range("5", "0"), range("5", "6"), range("6", ""), range("", "0"));
        testGRR(range("7", "2"), range("7", ""), range("", "1"), range("1", "2"));
        testGRR(range("0", "0"), range("0", "1"), range("1", "6"), range("6", ""), range("", "0"));
        testGRR(range("", ""), range("", "1"), range("1", "6"), range("6", ""));
        testGRR(range("6", "6"), range("6", ""), range("", "1"), range("1", "6"));
        testGRR(range("6", "1"), range("6", ""), range("", "1"));
        testGRR(range("5", ""), range("5", "6"), range("6", ""));
    }
    @Test
    public void testGRRExactBounds() throws Throwable
    {
        testGRR(bounds("0", "0"), bounds("0", "0"));
        testGRR(bounds("", ""), bounds("", "1"), range("1", "6"), range("6", ""));
    }
}
