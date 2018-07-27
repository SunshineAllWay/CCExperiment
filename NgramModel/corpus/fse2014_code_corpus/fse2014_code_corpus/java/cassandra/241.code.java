package org.apache.cassandra.gms;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import org.apache.cassandra.io.ICompactSerializer;
import org.apache.cassandra.net.CompactEndpointSerializationHelper;
public class GossipDigest implements Comparable<GossipDigest>
{
    private static ICompactSerializer<GossipDigest> serializer_;
    static
    {
        serializer_ = new GossipDigestSerializer();
    }
    InetAddress endpoint_;
    int generation_;
    int maxVersion_;
    public static ICompactSerializer<GossipDigest> serializer()
    {
        return serializer_;
    }
    GossipDigest(InetAddress endpoint, int generation, int maxVersion)
    {
        endpoint_ = endpoint;
        generation_ = generation; 
        maxVersion_ = maxVersion;
    }
    InetAddress getEndpoint()
    {
        return endpoint_;
    }
    int getGeneration()
    {
        return generation_;
    }
    int getMaxVersion()
    {
        return maxVersion_;
    }
    public int compareTo(GossipDigest gDigest)
    {
        if ( generation_ != gDigest.generation_ )
            return ( generation_ - gDigest.generation_ );
        return (maxVersion_ - gDigest.maxVersion_);
    }
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(endpoint_);
        sb.append(":");
        sb.append(generation_);
        sb.append(":");
        sb.append(maxVersion_);
        return sb.toString();
    }
}
class GossipDigestSerializer implements ICompactSerializer<GossipDigest>
{       
    public void serialize(GossipDigest gDigest, DataOutputStream dos) throws IOException
    {        
        CompactEndpointSerializationHelper.serialize(gDigest.endpoint_, dos);
        dos.writeInt(gDigest.generation_);
        dos.writeInt(gDigest.maxVersion_);
    }
    public GossipDigest deserialize(DataInputStream dis) throws IOException
    {
        InetAddress endpoint = CompactEndpointSerializationHelper.deserialize(dis);
        int generation = dis.readInt();
        int version = dis.readInt();
        return new GossipDigest(endpoint, generation, version);
    }
}
