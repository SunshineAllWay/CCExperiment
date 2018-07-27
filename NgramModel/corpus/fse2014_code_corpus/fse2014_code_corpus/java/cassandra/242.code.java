package org.apache.cassandra.gms;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import org.apache.cassandra.io.ICompactSerializer;
class GossipDigestAck2Message
{
    private static  ICompactSerializer<GossipDigestAck2Message> serializer_;
    static
    {
        serializer_ = new GossipDigestAck2MessageSerializer();
    }
    Map<InetAddress, EndpointState> epStateMap_ = new HashMap<InetAddress, EndpointState>();
    public static ICompactSerializer<GossipDigestAck2Message> serializer()
    {
        return serializer_;
    }
    GossipDigestAck2Message(Map<InetAddress, EndpointState> epStateMap)
    {
        epStateMap_ = epStateMap;
    }
    Map<InetAddress, EndpointState> getEndpointStateMap()
    {
         return epStateMap_;
    }
}
class GossipDigestAck2MessageSerializer implements ICompactSerializer<GossipDigestAck2Message>
{
    public void serialize(GossipDigestAck2Message gDigestAck2Message, DataOutputStream dos) throws IOException
    {
        EndpointStatesSerializationHelper.serialize(gDigestAck2Message.epStateMap_, dos);
    }
    public GossipDigestAck2Message deserialize(DataInputStream dis) throws IOException
    {
        Map<InetAddress, EndpointState> epStateMap = EndpointStatesSerializationHelper.deserialize(dis);
        return new GossipDigestAck2Message(epStateMap);        
    }
}
