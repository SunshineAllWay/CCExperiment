package org.apache.cassandra.gms;
import static org.junit.Assert.*;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import org.apache.cassandra.io.util.DataOutputBuffer;
import java.net.InetAddress;
import org.junit.Test;
public class GossipDigestTest
{
    @Test
    public void test() throws IOException
    {
        InetAddress endpoint = InetAddress.getByName("127.0.0.1");
        int generation = 0;
        int maxVersion = 123;
        GossipDigest expected = new GossipDigest(endpoint, generation, maxVersion);
        assertEquals(endpoint, expected.getEndpoint());
        assertEquals(generation, expected.getGeneration());
        assertEquals(maxVersion, expected.getMaxVersion());
        DataOutputBuffer output = new DataOutputBuffer();
        GossipDigest.serializer().serialize(expected, output);
        ByteArrayInputStream input = new ByteArrayInputStream(output.getData(), 0, output.getLength());
        GossipDigest actual = GossipDigest.serializer().deserialize(new DataInputStream(input));
        assertEquals(0, expected.compareTo(actual));
    }
}
